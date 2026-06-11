package com.agent.agent;

import com.agent.rag.VectorStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 知识检索 Agent：调用向量库做 Top-K 相似度检索，拼接为上下文。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RetrievalAgent {

    private final VectorStoreService vectorStoreService;

    @Value("${agent.rag.top-k:5}")
    private int topK;

    /**
     * 检索与问题相关的知识片段，返回拼接文本（无结果返回空串）。
     */
    public String retrieve(String query) {
        List<String> chunks = vectorStoreService.search(query, topK);
        if (chunks == null || chunks.isEmpty()) {
            log.debug("RAG 未检索到相关内容 query={}", query);
            return "";
        }
        log.debug("RAG 命中 {} 条 query={}", chunks.size(), query);
        return String.join("\n---\n", chunks);
    }

    /**
     * 是否检索到有效内容。
     */
    public boolean hasContext(String query) {
        return !retrieve(query).isBlank();
    }
}
