package com.tlcsdm.common.publish;

import com.tlcsdm.common.domain.LogDocument;
import com.tlcsdm.common.event.LogEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: 唐 亮
 * @date: 2022/3/13 0:27
 * @since: 1.0
 */
@Slf4j
public class ConsolePrintLog extends AbtractLogEventHandle {

    /**
     * 控制台打印日志
     * 日志信息事件中日志对象为null不处理
     *
     * @param logEvent 日志信息事件
     */
    @Override
    public void onApplicationEvent(LogEvent logEvent) {
        LogDocument logDocument = logEvent.getLogDocument();
        if (logDocument.getSuccess() == 0) {
            log.error("traceid: [{}] 模块名: [{}]  操作类型: [{}]  IP: [{}]  URL: [{}]  参数: [{}]  操作人: [{}] 错误信息: [{}]", logDocument.getTraceId(), logDocument.getTitle(), logDocument.getOperateType(), logDocument.getIp(), logDocument.getUrl(), logDocument.getParams(), logDocument.getOperatePer(), logDocument.getErrMessage());
        } else {
            log.info("模块名: [{}]  操作类型: [{}]  IP: [{}]  URL: [{}]  参数: [{}]  操作人: [{}]", logDocument.getTitle(), logDocument.getOperateType(), logDocument.getIp(), logDocument.getUrl(), logDocument.getParams(), logDocument.getOperatePer());
        }
    }
}
