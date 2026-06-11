package com.agent.rag;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * pgvector 向量写入与检索。
 *
 * <p>直接使用 JdbcTemplate 操作 kb_chunk 表，余弦距离算子 {@code <=>} 检索 Top-K。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VectorStoreService {

    @Qualifier("vectorJdbcTemplate")
    private final JdbcTemplate vectorJdbc;

    private final EmbeddingModel embeddingModel;

    @Value("${agent.rag.top-k:5}")
    private int defaultTopK;

    /**
     * 将文档分块向量化并写入 kb_chunk。
     */
    @Transactional
    public void saveChunks(Long docId, List<String> chunks) {
        String sql = "INSERT INTO kb_chunk (doc_id, chunk_index, content, embedding) "
                + "VALUES (?, ?, ?, ?::vector)";
        for (int i = 0; i < chunks.size(); i++) {
            String content = chunks.get(i);
            Embedding embedding = embeddingModel.embed(TextSegment.from(content)).content();
            String vectorLiteral = toVectorLiteral(embedding.vector());
            vectorJdbc.update(sql, docId, i, content, vectorLiteral);
        }
        log.info("向量写入完成 docId={} count={}", docId, chunks.size());
    }

    /**
     * 相似度检索，返回 Top-K 文本片段。
     */
    public List<String> search(String query, int topK) {
        Embedding queryEmbedding = embeddingModel.embed(query).content();
        String vectorLiteral = toVectorLiteral(queryEmbedding.vector());
        String sql = "SELECT content FROM kb_chunk ORDER BY embedding <=> ?::vector LIMIT ?";
        return vectorJdbc.queryForList(sql, String.class, vectorLiteral, topK);
    }

    /**
     * 使用默认 Top-K 检索。
     */
    public List<String> search(String query) {
        return search(query, defaultTopK);
    }

    /**
     * 删除某文档的全部向量块。
     */
    public void deleteByDocId(Long docId) {
        vectorJdbc.update("DELETE FROM kb_chunk WHERE doc_id = ?", docId);
    }

    /**
     * 查询某文档已存储的全部分块（按顺序），用于展示"向量库里到底存了什么"。
     */
    public List<java.util.Map<String, Object>> listChunks(Long docId) {
        return vectorJdbc.queryForList(
                "SELECT chunk_index, content FROM kb_chunk WHERE doc_id = ? ORDER BY chunk_index",
                docId);
    }

    /**
     * float[] 转 pgvector 字面量，形如 "[0.1,0.2,...]"。
     */
    private String toVectorLiteral(float[] vector) {
        StringBuilder sb = new StringBuilder(vector.length * 8);
        sb.append('[');
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(vector[i]);
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * 未使用，保留以示完整：批量检索返回带内容的段。
     */
    @SuppressWarnings("unused")
    private List<TextSegment> toSegments(List<String> contents) {
        List<TextSegment> segments = new ArrayList<>(contents.size());
        for (String c : contents) {
            segments.add(TextSegment.from(c));
        }
        return segments;
    }
}
