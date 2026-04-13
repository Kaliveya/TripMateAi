package com.sean.trip.tripmateai.config;

import com.sean.trip.tripmateai.store.PostgresChatMemoryStore;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;

/**
 * 对话记忆持久化配置类
 * 需实现 ChatMemoryStore（PostgresChatMemoryStore）
 */
@Configuration
public class TripPlannerConfig {

    @Bean
    public ChatMemoryProvider chatMemoryProvider(PostgresChatMemoryStore postgresStore) {
        return memoryId -> TokenWindowChatMemory.builder()
                .id(memoryId)
                .maxTokens(40000, new OpenAiTokenCountEstimator(GPT_4_O_MINI))//达到token限制时evict旧消息
                .chatMemoryStore(postgresStore)
                .build();
    }

}
