package com.agent.rag;

import com.agent.entity.KbDocument;
import com.agent.mapper.KbDocumentMapper;
import com.agent.service.KnowledgeService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 知识库内置文档自动入库。
 *
 * <p>应用启动后，把 resources/docs 下的内置文档解析、向量化并写入向量库，
 * 使知识库开箱即用、可直接 RAG 检索。已索引（chunk_count&gt;0）则跳过，幂等。
 * 若 OpenAI embedding 不可达（如未挂代理），记录告警并跳过，不影响应用启动。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeSeeder implements ApplicationRunner {

    private final KbDocumentMapper kbDocumentMapper;
    private final KnowledgeService knowledgeService;

    /** 内置文档：标题 / 类型 / classpath 路径 */
    private record Seed(String title, String fileType, String path) {
    }

    private static final List<Seed> SEEDS = List.of(
            new Seed("退换货政策", "markdown", "docs/refund-policy.md"),
            new Seed("物流时效说明", "txt", "docs/logistics.txt")
    );

    @Override
    public void run(ApplicationArguments args) {
        for (Seed seed : SEEDS) {
            try {
                if (alreadyIndexed(seed.title())) {
                    log.info("知识库已存在，跳过自动入库：{}", seed.title());
                    continue;
                }
                String content = readResource(seed.path());
                knowledgeService.indexText(seed.title(), seed.fileType(), seed.path(), content);
                log.info("知识库内置文档已自动入库：{}", seed.title());
            } catch (Exception e) {
                log.warn("知识库内置文档自动入库失败（可能未挂代理/OpenAI 不可达），已跳过：{} -> {}",
                        seed.title(), e.getMessage());
            }
        }
    }

    /**
     * 是否已索引（存在同名文档且分块数 > 0）。
     */
    private boolean alreadyIndexed(String title) {
        Long count = kbDocumentMapper.selectCount(Wrappers.<KbDocument>lambdaQuery()
                .eq(KbDocument::getTitle, title)
                .gt(KbDocument::getChunkCount, 0));
        return count != null && count > 0;
    }

    private String readResource(String path) throws Exception {
        try (InputStream in = new ClassPathResource(path).getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
