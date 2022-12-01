package com.tlcsdm.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author: 唐 亮
 * @date: 2022/12/1 21:52
 */
public class Builder<T> {

    /**
     * 存储调用方 指定构造类的 构造器
     */
    private final Supplier<T> constructor;
    /**
     * 存储 指定类 所有需要初始化的类属性
     */
    private final List<Consumer<T>> dInjects = new ArrayList<>();

    private Consumer head = new Consumer() {
        @Override
        public void accept(Object o) {

        }
    };

    private Builder(Supplier<T> constructor) {
        this.constructor = constructor;
    }

    public static <T> Builder<T> builder(Supplier<T> constructor) {
        return new Builder<>(constructor);
    }

    public <P1> Builder<T> with(Builder.DInjectConsumer<T, P1> consumer, P1 p1) {
        Consumer<T> c = instance -> consumer.accept(instance, p1);
        head = head.andThen(c);
        return this;
    }

    public <P1> Builder<T> with(Builder.DInjectConsumer<T, P1> consumer, P1 p1, Predicate<P1> predicate) {
        if (null != predicate && !predicate.test(p1)) {
            throw new RuntimeException(String.format("【%s】Parameter does not comply with the rules!", p1));
        }
        Consumer<T> c = instance -> consumer.accept(instance, p1);
        head = head.andThen(c);
        return this;
    }

    public <P1, P2> Builder<T> with(Builder.DInjectConsumer2<T, P1, P2> consumer, P1 p1, P2 p2) {
        Consumer<T> c = instance -> consumer.accept(instance, p1, p2);
        head = head.andThen(c);
        return this;
    }

    public T build() {
        // 调用supplier 生成类实例
        T instance = constructor.get();
        // 调用传入的setter方法，完成属性初始化
        head.accept(instance);
        return instance;
    }

    @FunctionalInterface
    public interface DInjectConsumer<T, P1> {
        void accept(T t, P1 p1);
    }

    @FunctionalInterface
    public interface DInjectConsumer2<T, P1, P2> {
        void accept(T t, P1 p1, P2 p2);
    }

}
