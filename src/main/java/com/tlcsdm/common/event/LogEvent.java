package com.tlcsdm.common.event;

import com.tlcsdm.common.domain.LogDocument;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 日志信息事件
 *
 * @author: TangLiang
 * @date: 2021/4/16 14:16
 * @since: 1.0
 */
@Getter
public class LogEvent extends ApplicationEvent {
    private LogDocument logDocument;

    public LogEvent(Object source, LogDocument logDocument) {
        super(source);
        this.logDocument = logDocument;
    }
}
