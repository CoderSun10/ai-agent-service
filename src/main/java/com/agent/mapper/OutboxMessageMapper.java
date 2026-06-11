package com.agent.mapper;

import com.agent.entity.OutboxMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Outbox 消息 Mapper。
 */
@Mapper
public interface OutboxMessageMapper extends BaseMapper<OutboxMessage> {

    /**
     * 查询待发送消息（status=0），限量。
     */
    List<OutboxMessage> selectPending(@Param("limit") int limit);

    /**
     * 标记为已发送。
     */
    int markSent(@Param("id") Long id);

    /**
     * 累加重试次数。
     */
    int incrRetry(@Param("id") Long id);
}
