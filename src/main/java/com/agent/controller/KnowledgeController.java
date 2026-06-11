package com.agent.controller;

import com.agent.common.Result;
import com.agent.entity.KbDocument;
import com.agent.service.KnowledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 知识库接口。
 */
@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    /**
     * 上传文档并建立索引。
     * POST /api/knowledge/upload  (multipart/form-data: file, title?)
     */
    @PostMapping("/upload")
    public Result<Long> upload(@RequestParam("file") MultipartFile file,
                               @RequestParam(value = "title", required = false) String title) {
        Long docId = knowledgeService.uploadAndIndex(file, title);
        return Result.success("索引成功", docId);
    }

    /**
     * 文档列表。
     */
    @GetMapping("/list")
    public Result<List<KbDocument>> list() {
        return Result.success(knowledgeService.listAll());
    }

    /**
     * 查看某文档在向量库中存储的分块内容。
     */
    @GetMapping("/chunks/{docId}")
    public Result<List<java.util.Map<String, Object>>> chunks(@PathVariable Long docId) {
        return Result.success(knowledgeService.listChunks(docId));
    }
}
