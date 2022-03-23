package com.tlcsdm.common.exception;

/**
 * 批量新增异常
 *
 * @author: TangLiang
 * @date: 2021/7/14 10:48
 * @since: 1.0
 */
public class InsertbatchException extends RuntimeException {
    public InsertbatchException() {
    }

    public InsertbatchException(String message) {
        super(message);
    }

    public InsertbatchException(String message, Throwable cause) {
        super(message, cause);
    }

    public InsertbatchException(Throwable cause) {
        super(cause);
    }
}
