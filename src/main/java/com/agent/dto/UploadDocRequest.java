package com.agent.dto;

import lombok.Data;

/**
 * 文档上传元数据（MultipartFile 单独作为接口参数传入）。
 */
@Data
public class UploadDocRequest {

    /** 文档标题，为空时取文件名 */
    private String title;
}
