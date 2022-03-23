package com.tlcsdm.common.publish;

import com.tlcsdm.common.event.LogEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

/**
 * @author: 唐 亮
 * @date: 2022/3/13 17:56
 * @since: 1.0
 */
public abstract class AbtractLogEventHandle {

    @Async
    @EventListener(condition = "#logEvent.logDocument != null")
    public abstract void onApplicationEvent(LogEvent logEvent);
}
