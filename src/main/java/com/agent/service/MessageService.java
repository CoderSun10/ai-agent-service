package com.agent.service;

import com.agent.common.enums.MessageRole;
import com.agent.entity.Message;
import com.agent.mapper.MessageMapper;
import com.agent.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 消息持久化服务（MySQL 落库，区别于 Redis 短期记忆）。
 */
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageMapper messageMapper;

    /**
     * 保存一问一答。
     */
    @Transactional
    public void savePair(String convId, String userMsg, String assistantMsg) {
        messageMapper.insert(build(convId, MessageRole.USER.getValue(), userMsg, null));
        messageMapper.insert(build(convId, MessageRole.ASSISTANT.getValue(), assistantMsg, null));
    }

    /**
     * 保存单条消息。
     */
    public void saveOne(String convId, MessageRole role, String content, String toolName) {
        messageMapper.insert(build(convId, role.getValue(), content, toolName));
    }

    /**
     * 查询会话全部消息。
     */
    public List<MessageVO> listByConvId(String convId) {
        return messageMapper.selectByConvId(convId).stream()
                .map(m -> new MessageVO(m.getRole(), m.getContent(), m.getToolName(), m.getCreateTime()))
                .toList();
    }

    private Message build(String convId, String role, String content, String toolName) {
        Message m = new Message();
        m.setConversationId(convId);
        m.setRole(role);
        m.setContent(content);
        m.setToolName(toolName);
        return m;
    }
}
