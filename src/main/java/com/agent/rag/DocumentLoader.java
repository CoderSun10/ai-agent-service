package com.agent.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 文档解析与分块。
 *
 * <p>支持 PDF / TXT / Markdown，使用 LangChain4j 递归分块器
 * （按段落语义优先切割，保留语义完整性，而非定长截断）。
 */
@Slf4j
@Component
public class DocumentLoader {

    @Value("${agent.rag.chunk-size:500}")
    private int chunkSize;

    @Value("${agent.rag.chunk-overlap:50}")
    private int chunkOverlap;

    /**
     * 解析并分块，返回纯文本片段列表。
     */
    public List<String> loadAndChunk(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        DocumentParser parser = fileName.endsWith(".pdf")
                ? new ApachePdfBoxDocumentParser()
                : new TextDocumentParser();

        Document document;
        try (InputStream in = file.getInputStream()) {
            document = parser.parse(in);
        }

        List<String> chunks = split(document);
        log.info("文档解析完成 file={} 分块数={}", fileName, chunks.size());
        return chunks;
    }

    /**
     * 直接对一段纯文本分块（用于知识库内置文档自动入库）。
     */
    public List<String> chunk(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return split(Document.from(text));
    }

    private List<String> split(Document document) {
        DocumentSplitter splitter = DocumentSplitters.recursive(chunkSize, chunkOverlap);
        return splitter.split(document).stream()
                .map(TextSegment::text)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
