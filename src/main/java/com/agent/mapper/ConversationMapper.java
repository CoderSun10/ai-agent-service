package com.agent.mapper;

import com.agent.entity.Conversation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 会话 Mapper。
 */
@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {

    /**
     * 更新会话状态。
     *
     * @param convId 会话 ID
     * @param status 新状态（见 ConversationStatus）
     * @return 受影响行数
     */
    int updateStatus(@Param("convId") String convId, @Param("status") int status);

    /**
     * 标记低置信。
     */
    int markConfidenceLow(@Param("convId") String convId);

    /**
     * 查询某用户的会话列表（按创建时间倒序）。
     */
    List<Conversation> selectByUserId(@Param("userId") Long userId);
}
