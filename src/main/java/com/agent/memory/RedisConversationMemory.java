package com.agent.memory;

import com.agent.common.constant.RedisKey;
import com.agent.common.util.TokenCounter;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.data.message.UserMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于 Redis List 的对话短期记忆。
 *
 * <p>每个元素是 LangChain4j {@link ChatMessage} 的 JSON 串。
 * 写入时按「条数 + Token」双阈值做滑动窗口裁剪，控制注入 Prompt 的上下文规模。
 */
@Service
@RequiredArgsConstructor
public class RedisConversationMemory {

    private final StringRedisTemplate redis;

    @Value("${agent.memory.max-history:20}")
    private int maxHistory;

    @Value("${agent.memory.max-tokens:3000}")
    private int maxTokens;

    /**
     * 加载历史消息（按时间正序）。
     */
    public List<ChatMessage> loadHistory(String convId) {
        String key = RedisKey.history(convId);
        List<String> raw = redis.opsForList().range(key, 0, -1);
        if (raw == null || raw.isEmpty()) {
            return new ArrayList<>();
        }
        List<ChatMessage> messages = new ArrayList<>(raw.size());
        for (String json : raw) {
            messages.add(ChatMessageDeserializer.messageFromJson(json));
        }
        return messages;
    }

    /**
     * 保存一轮对话（用户消息 + AI 回复），并做滑动窗口裁剪。
     */
    public void save(String convId, String userMsg, String assistantMsg) {
        String key = RedisKey.history(convId);
        redis.opsForList().rightPush(key, ChatMessageSerializer.messageToJson(UserMessage.from(userMsg)));
        redis.opsForList().rightPush(key, ChatMessageSerializer.messageToJson(AiMessage.from(assistantMsg)));
        redis.expire(key, RedisKey.HISTORY_TTL);
        trimToWindow(convId);
    }

    /**
     * 滑动窗口裁剪：先按条数裁，再按 Token 裁。每次裁掉最旧的一问一答。
     */
    public void trimToWindow(String convId) {
        String key = RedisKey.history(convId);

        // 1. 条数裁剪
        Long size = redis.opsForList().size(key);
        if (size != null && size > maxHistory * 2L) {
            redis.opsForList().trim(key, size - maxHistory * 2L, -1);
        }

        // 2. Token 裁剪
        List<String> raw = redis.opsForList().range(key, 0, -1);
        if (raw == null || raw.isEmpty()) {
            return;
        }
        int totalTokens = raw.stream().mapToInt(TokenCounter::estimate).sum();
        int removed = 0;
        while (totalTokens > maxTokens && raw.size() - removed > 2) {
            // 裁掉最旧的一问一答
            totalTokens -= TokenCounter.estimate(raw.get(removed));
            totalTokens -= TokenCounter.estimate(raw.get(removed + 1));
            removed += 2;
        }
        if (removed > 0) {
            redis.opsForList().trim(key, removed, -1);
        }
    }

    /**
     * 清空会话历史。
     */
    public void clear(String convId) {
        redis.delete(RedisKey.history(convId));
        redis.delete(RedisKey.tokens(convId));
    }
}
