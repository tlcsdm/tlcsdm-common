package com.tlcsdm.common.exception;

/**
 * feign调用异常
 *
 * @author: TangLiang
 * @date: 2022/1/7 9:21
 * @since: 1.0
 */
public class FeignTransferException extends RuntimeException {
    public FeignTransferException() {
    }

    public FeignTransferException(String message) {
        super(message);
    }

    public FeignTransferException(String message, Throwable cause) {
        super(message, cause);
    }

    public FeignTransferException(Throwable cause) {
        super(cause);
    }
}
