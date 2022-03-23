package com.tlcsdm.common.exception;

/**
 * sql执行失败异常
 *
 * @author: TangLiang
 * @date: 2021/7/14 10:48
 * @since: 1.0
 */
public class SqlExecuteFailException extends RuntimeException {
    public SqlExecuteFailException() {
    }

    public SqlExecuteFailException(String message) {
        super(message);
    }

    public SqlExecuteFailException(String message, Throwable cause) {
        super(message, cause);
    }

    public SqlExecuteFailException(Throwable cause) {
        super(cause);
    }
}
