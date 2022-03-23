package com.tlcsdm.common.util;

import com.tlcsdm.common.base.BaseUtils;
import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author: TangLiang
 * @date: 2022/1/20 14:22
 * @since: 1.0
 */
public class ThreadMdcUtil {
    public static void setTraceIdIfAbsent() {
        if (MDC.get(BaseUtils.TRACE_ID) == null) {
            MDC.put(BaseUtils.TRACE_ID, BaseUtils.getUuid());
        }
    }

    public static <T> Callable<T> wrap(final Callable<T> callable, final Map<String, String> context) {
        return () -> {
            if (context == null) {
                MDC.clear();
            } else {
                MDC.setContextMap(context);
            }
            setTraceIdIfAbsent();
            try {
                return callable.call();
            } finally {
                MDC.clear();
            }
        };
    }

    public static Runnable wrap(final Runnable runnable, final Map<String, String> context) {
        return () -> {
            if (context == null) {
                MDC.clear();
            } else {
                MDC.setContextMap(context);
            }
            setTraceIdIfAbsent();
            try {
                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }
}
