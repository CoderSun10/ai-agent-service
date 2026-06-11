package com.agent.service;

import com.agent.common.ResultCode;
import com.agent.common.constant.RedisKey;
import com.agent.common.enums.ConversationStatus;
import com.agent.common.exception.BizException;
import com.agent.entity.Conversation;
import com.agent.mapper.ConversationMapper;
import com.agent.vo.ConversationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 会话服务。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationMapper conversationMapper;
    private final StringRedisTemplate redis;

    /**
     * 新建会话。
     */
    public String create(Long userId) {
        Conversation conv = new Conversation();
        conv.setUserId(userId);
        conv.setStatus(ConversationStatus.IN_PROGRESS.getCode());
        conv.setConfidenceLow(0);
        conversationMapper.insert(conv);

        // 记录用户当前在线会话
        redis.opsForValue().set(
                RedisKey.userConversation(userId),
                conv.getId(),
                RedisKey.USER_CONVERSATION_TTL);

        log.info("新建会话 convId={} userId={}", conv.getId(), userId);
        return conv.getId();
    }

    /**
     * 校验会话存在且属于当前用户，返回会话；不存在抛业务异常。
     */
    public Conversation requireOwned(String convId, Long userId) {
        Conversation conv = conversationMapper.selectById(convId);
        if (conv == null || conv.getDeleted() != null && conv.getDeleted() == 1) {
            throw new BizException(ResultCode.CONVERSATION_NOT_FOUND);
        }
        if (!conv.getUserId().equals(userId)) {
            throw new BizException(ResultCode.FORBIDDEN, "无权访问该会话");
        }
        return conv;
    }

    /**
     * 若 convId 为空则新建，否则校验归属后返回原 ID。
     */
    public String createIfAbsent(String convId, Long userId) {
        if (convId == null || convId.isBlank()) {
            return create(userId);
        }
        requireOwned(convId, userId);
        return convId;
    }

    /**
     * 结束会话。
     */
    public void end(String convId, Long userId) {
        requireOwned(convId, userId);
        conversationMapper.updateStatus(convId, ConversationStatus.ENDED.getCode());
    }

    /**
     * 列出用户的所有会话。
     */
    public List<ConversationVO> listByUser(Long userId) {
        return conversationMapper.selectByUserId(userId).stream()
                .map(c -> new ConversationVO(
                        c.getId(),
                        c.getStatus(),
                        ConversationStatus.of(c.getStatus()).getDesc(),
                        c.getCreateTime()))
                .toList();
    }
}
