-- ============================================================
-- PostgreSQL + pgvector 向量库初始化
-- ============================================================
CREATE EXTENSION IF NOT EXISTS vector;

-- 知识块向量表
CREATE TABLE IF NOT EXISTS kb_chunk (
    id          BIGSERIAL PRIMARY KEY,
    doc_id      BIGINT      NOT NULL,                 -- 关联 MySQL kb_document.id
    chunk_index INT         NOT NULL,                 -- 第几个分块
    content     TEXT        NOT NULL,                 -- 原始文本
    embedding   vector(1536),                         -- 向量维度（text-embedding-3-small）
    create_time TIMESTAMP   DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_kb_chunk_doc_id ON kb_chunk (doc_id);

-- 向量检索索引（HNSW + 余弦距离）
CREATE INDEX IF NOT EXISTS idx_kb_chunk_embedding
    ON kb_chunk USING hnsw (embedding vector_cosine_ops);
