package com.sean.trip.tripmateai.store;

import cn.hutool.core.util.ObjectUtil;
import com.sean.trip.tripmateai.domain.entity.ChatMemoryEntity;
import com.sean.trip.tripmateai.mapper.ChatMemoryMapper;
import dev.langchain4j.data.message.*;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PostgresChatMemoryStore implements ChatMemoryStore {

    @Autowired
    private ChatMemoryMapper chatMemoryMapper;

    /**
     * 重写getMessage方法获取会话消息
     * @param memoryId
     * @return
     */
    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        ChatMemoryEntity entity = chatMemoryMapper.findBySessionId(memoryId.toString());

        // 查不到记录，返回空列表
        if (ObjectUtil.isEmpty(entity)) {
            return new ArrayList<>();
        }
        return ChatMessageDeserializer.messagesFromJson(entity.getContent());
    }

    /**
     * 重写updateMessage方法更新会话消息
     * @param memoryId
     * @param messages
     */
    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String sessionId = memoryId.toString();
        // 不过滤，直接存
        String json = ChatMessageSerializer.messagesToJson(messages);

        ChatMemoryEntity entity = chatMemoryMapper.findBySessionId(sessionId);
        if (ObjectUtil.isEmpty(entity)) {
            entity = new ChatMemoryEntity();
        }
        entity.setSessionId(sessionId);
        entity.setContent(json);
        if (entity.getId() == null) {
            chatMemoryMapper.insert(entity);
        } else {
            chatMemoryMapper.updateById(entity);
        }
    }
    /**
     * 删除会话消息
     * @param memoryId
     */
    @Override
    public void deleteMessages(Object memoryId) {
        chatMemoryMapper.deleteBySessionId(memoryId.toString());
    }
}
