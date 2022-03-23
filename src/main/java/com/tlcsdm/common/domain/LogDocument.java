package com.tlcsdm.common.domain;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * 日志封装类
 *
 * @author: TangLiang
 * @date: 2021/4/16 14:16
 * @since: 1.0
 */
@Data
@Builder
@Slf4j
public class LogDocument {
    /**
     * 服务名
     */
    private String service;

    /**
     * 模块名
     */
    private String title;

    /**
     * 服务主机名
     */
    private String hostName;

    /**
     * 操作人
     */
    private String operatePer;

    /**
     * 客户端ip
     */
    private String ip;

    /**
     * 浏览器信息
     */
    private String browser;

    /**
     * 浏览器版本
     */
    private String version;

    /**
     * 操作系统信息
     */
    private String os;

    /**
     * 操作类型
     */
    private String operateType;

    /**
     * 请求路径
     */
    private String url;

    /**
     * 请求方法名
     */
    private String signature;

    /**
     * 请求参数
     */
    private String params;

    /**
     * 操作时间
     */
    private LocalDateTime createTime;

    /**
     * 成功1, 失败 0
     */
    private long success;

    /**
     * 项目版本
     */
    private String projectVersion;

    /**
     * traceId
     */
    private String traceId;

    /**
     * 错误信息
     */
    private String errMessage;

    /**
     * 结果
     */
    private String result;
}
