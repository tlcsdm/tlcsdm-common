package com.tlcsdm.common.annotation;

import java.lang.annotation.*;

/**
 * 接口日志注解
 *
 * @author: TangLiang
 * @date: 2021/4/16 13:44
 * @since: 1.0
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiLog {
    /**
     * 模块
     */
    String title() default "";

    /**
     * 操作类型
     */
    String operateType() default "";
}
