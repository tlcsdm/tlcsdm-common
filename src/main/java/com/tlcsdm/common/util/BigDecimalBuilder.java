package com.tlcsdm.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * BigDecimal建造类
 * 主要用于批量计算
 *
 * @author: TangLiang
 * @date: 2021/12/22 8:35
 * @since 1.1
 */
public class BigDecimalBuilder {
    private BigDecimal bigDecimal;
    /**
     * 默认除法运算精度
     */
    private final int DEF_DIV_SCALE = 2;

    //构造器
    private BigDecimalBuilder(int i) {
        this.bigDecimal = new BigDecimal(i);
    }

    private BigDecimalBuilder(String str) {
        this.bigDecimal = new BigDecimal(str);
    }

    private BigDecimalBuilder(double d) {
        this.bigDecimal = new BigDecimal(d);
    }

    private void set(BigDecimal decimal) {
        this.bigDecimal = decimal;
    }

    public static BigDecimalBuilder builder(int i) {
        return new BigDecimalBuilder(i);
    }

    public static BigDecimalBuilder builder(String str) {
        return new BigDecimalBuilder(str);
    }

    public static BigDecimalBuilder builder(double d) {
        return new BigDecimalBuilder(d);
    }

    public BigDecimalBuilder add(String... strs) {
        for (String str : strs) {
            set(bigDecimal.add(new BigDecimal(str)));
        }
        return this;
    }

    public BigDecimalBuilder add(double... doubles) {
        for (double d : doubles) {
            set(bigDecimal.add(BigDecimal.valueOf(d)));
        }
        return this;
    }

    public BigDecimalBuilder add(int... ints) {
        for (int i : ints) {
            set(bigDecimal.add(BigDecimal.valueOf(i)));
        }
        return this;
    }

    public BigDecimalBuilder sub(String... strs) {
        for (String str : strs) {
            set(bigDecimal.subtract(new BigDecimal(str)));
        }
        return this;
    }

    public BigDecimalBuilder sub(double... doubles) {
        for (double d : doubles) {
            set(bigDecimal.subtract(BigDecimal.valueOf(d)));
        }
        return this;
    }

    public BigDecimalBuilder sub(int... ints) {
        for (int i : ints) {
            set(bigDecimal.subtract(BigDecimal.valueOf(i)));
        }
        return this;
    }

    public BigDecimalBuilder mul(String... strs) {
        for (String str : strs) {
            set(bigDecimal.multiply(new BigDecimal(str)));
        }
        return this;
    }

    public BigDecimalBuilder mul(double... doubles) {
        for (double d : doubles) {
            set(bigDecimal.multiply(BigDecimal.valueOf(d)));
        }
        return this;
    }

    public BigDecimalBuilder mul(int... ints) {
        for (int i : ints) {
            set(bigDecimal.multiply(BigDecimal.valueOf(i)));
        }
        return this;
    }

    /**
     * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指
     * 定精度(默认2位)，以后的数字四舍五入。
     */
    public BigDecimalBuilder div(String... strs) {
        div(DEF_DIV_SCALE, strs);
        return this;
    }

    public BigDecimalBuilder div(double... doubles) {
        div(DEF_DIV_SCALE, doubles);
        return this;
    }

    public BigDecimalBuilder div(int scale, String... strs) {
        for (String str : strs) {
            set(bigDecimal.divide(new BigDecimal(str), scale, RoundingMode.HALF_UP));
        }
        return this;
    }

    public BigDecimalBuilder div(int scale, double... doubles) {
        for (double d : doubles) {
            set(bigDecimal.divide(BigDecimal.valueOf(d), scale, RoundingMode.HALF_UP));
        }
        return this;
    }

    /**
     * 由于需要传入小数精度，故int的类型入参不支持批量相除
     */
    public BigDecimalBuilder div(int scale, int i) {
        set(bigDecimal.divide(BigDecimal.valueOf(i), scale, RoundingMode.HALF_UP));
        return this;
    }

    /**
     * 提供精确的小数位四舍五入处理。
     *
     * @param scale 小数点后保留几位, 默认2位
     * @return 四舍五入后的结果
     */
    public BigDecimalBuilder round(int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException(
                    "The scale must be a positive integer or zero");
        }
        set(bigDecimal.divide(BigDecimal.ONE, scale, RoundingMode.HALF_UP));
        return this;
    }

    public BigDecimalBuilder round() {
        round(DEF_DIV_SCALE);
        return this;
    }

    public BigDecimal build() {
        return bigDecimal;
    }
}
