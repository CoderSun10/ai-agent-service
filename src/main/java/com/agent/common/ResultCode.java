package com.agent.common;

import lombok.Getter;

/**
 * 业务状态码枚举。
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无访问权限"),
    NOT_FOUND(404, "资源不存在"),
    ERROR(500, "系统内部错误"),

    // 业务自定义
    LOGIN_FAILED(1001, "用户名或密码错误"),
    CONVERSATION_NOT_FOUND(1002, "会话不存在"),
    LLM_CALL_FAILED(1003, "大模型调用失败"),
    RAG_INDEX_FAILED(1004, "知识库索引失败"),
    FILE_PARSE_FAILED(1005, "文档解析失败");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
