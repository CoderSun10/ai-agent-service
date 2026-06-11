package com.agent.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 会话视图对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationVO {

    private String id;
    private Integer status;
    private String statusDesc;
    private LocalDateTime createTime;
}
