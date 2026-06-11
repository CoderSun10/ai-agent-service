package com.agent.service;

import com.agent.common.ResultCode;
import com.agent.common.exception.BizException;
import com.agent.entity.KbDocument;
import com.agent.mapper.KbDocumentMapper;
import com.agent.rag.DocumentLoader;
import com.agent.rag.VectorStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 知识库服务：协调文档解析、向量写入与元数据落库。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final DocumentLoader documentLoader;
    private final VectorStoreService vectorStoreService;
    private final KbDocumentMapper kbDocumentMapper;

    /**
     * 上传并索引文档。
     *
     * @param file  上传文件（pdf / txt / markdown）
     * @param title 文档标题，可空（空则取文件名）
     * @return 文档 ID
     */
    @Transactional
    public Long uploadAndIndex(MultipartFile file, String title) {
        if (file == null || file.isEmpty()) {
            throw new BizException(ResultCode.BAD_REQUEST, "上传文件为空");
        }
        String fileName = file.getOriginalFilename() == null ? "untitled" : file.getOriginalFilename();
        String fileType = resolveType(fileName);
        String realTitle = (title == null || title.isBlank()) ? fileName : title;

        // 1. 先落元数据，拿到 docId
        KbDocument doc = new KbDocument();
        doc.setTitle(realTitle);
        doc.setFileType(fileType);
        doc.setFilePath(fileName);
        doc.setChunkCount(0);
        kbDocumentMapper.insert(doc);
        Long docId = doc.getId();

        // 2. 解析 + 分块
        List<String> chunks;
        try {
            chunks = documentLoader.loadAndChunk(file);
        } catch (Exception e) {
            log.error("文档解析失败 docId={}", docId, e);
            throw new BizException(ResultCode.FILE_PARSE_FAILED, "文档解析失败: " + e.getMessage());
        }
        if (chunks.isEmpty()) {
            throw new BizException(ResultCode.FILE_PARSE_FAILED, "文档无有效文本内容");
        }

        // 3. 向量化写入 pgvector
        try {
            vectorStoreService.saveChunks(docId, chunks);
        } catch (Exception e) {
            log.error("向量写入失败 docId={}", docId, e);
            throw new BizException(ResultCode.RAG_INDEX_FAILED, "向量写入失败: " + e.getMessage());
        }

        // 4. 回填分块数
        kbDocumentMapper.updateChunkCount(docId, chunks.size());
        log.info("知识库索引完成 docId={} title={} chunks={}", docId, realTitle, chunks.size());
        return docId;
    }

    /**
     * 直接用一段文本建立索引（用于内置文档自动入库）。
     *
     * @return 文档 ID
     */
    @Transactional
    public Long indexText(String title, String fileType, String filePath, String content) {
        List<String> chunks = documentLoader.chunk(content);
        if (chunks.isEmpty()) {
            throw new BizException(ResultCode.FILE_PARSE_FAILED, "文档无有效文本内容");
        }
        KbDocument doc = new KbDocument();
        doc.setTitle(title);
        doc.setFileType(fileType);
        doc.setFilePath(filePath);
        doc.setChunkCount(0);
        kbDocumentMapper.insert(doc);

        vectorStoreService.saveChunks(doc.getId(), chunks);
        kbDocumentMapper.updateChunkCount(doc.getId(), chunks.size());
        log.info("文本索引完成 title={} chunks={}", title, chunks.size());
        return doc.getId();
    }

    /**
     * 文档列表。
     */
    public List<KbDocument> listAll() {
        return kbDocumentMapper.selectList(null);
    }

    /**
     * 查询某文档在向量库中的分块内容。
     */
    public List<java.util.Map<String, Object>> listChunks(Long docId) {
        return vectorStoreService.listChunks(docId);
    }

    private String resolveType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".pdf")) {
            return "pdf";
        }
        if (lower.endsWith(".md") || lower.endsWith(".markdown")) {
            return "markdown";
        }
        return "txt";
    }
}
