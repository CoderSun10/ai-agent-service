package com.agent.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 消息视图对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageVO {

    private String role;
    private String content;
    private String toolName;
    private LocalDateTime createTime;
}
