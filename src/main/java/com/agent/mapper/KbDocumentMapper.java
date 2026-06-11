package com.agent.mapper;

import com.agent.entity.KbDocument;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 知识库文档 Mapper。
 */
@Mapper
public interface KbDocumentMapper extends BaseMapper<KbDocument> {

    @Update("UPDATE kb_document SET chunk_count = #{chunkCount} WHERE id = #{id}")
    int updateChunkCount(@Param("id") Long id, @Param("chunkCount") int chunkCount);
}
