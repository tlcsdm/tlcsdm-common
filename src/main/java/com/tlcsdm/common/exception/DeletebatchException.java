package com.tlcsdm.common.exception;

/**
 * 批量新增异常
 *
 * @author: TangLiang
 * @date: 2021/7/14 10:48
 * @since: 1.0
 */
public class DeletebatchException extends RuntimeException {
    public DeletebatchException() {
    }

    public DeletebatchException(String message) {
        super(message);
    }

    public DeletebatchException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeletebatchException(Throwable cause) {
        super(cause);
    }
}
