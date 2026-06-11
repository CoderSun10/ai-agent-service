package com.agent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 对话请求（POST 方式，SSE 接口走 GET 参数）。
 */
@Data
public class ChatRequest {

    /** 会话 ID，为空时由后端新建 */
    private String conversationId;

    @NotBlank(message = "消息内容不能为空")
    private String message;
}
