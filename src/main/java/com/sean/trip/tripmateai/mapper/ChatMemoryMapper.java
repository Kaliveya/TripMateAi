package com.sean.trip.tripmateai.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sean.trip.tripmateai.domain.entity.ChatMemoryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ChatMemoryMapper extends BaseMapper<ChatMemoryEntity> {

    /**
     * 根据会话ID查询会话消息
     * @param sessionId
     * @return
     */
    ChatMemoryEntity findBySessionId(@Param("sessionId") String sessionId);
    /**
     * 根据会话ID删除会话消息
     * @param sessionId
     */
    void deleteBySessionId(@Param("sessionId") String sessionId);


}
