package com.sean.trip.tripmateai.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallzh.BgeSmallZhEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class EmbeddingConfig {

    @Bean
    @ConditionalOnProperty(name = "embedding.mode", havingValue = "local", matchIfMissing = true)
    public EmbeddingModel localEmbeddingModel() {

        // BGE中文小模型，384维
        return new BgeSmallZhEmbeddingModel();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(DataSource dataSource) {
        return PgVectorEmbeddingStore.datasourceBuilder()
                .datasource(dataSource)
                .table("trip_knowledge")       // 表名，会自动创建
                .dimension(512)                // 本地模型521维
                .createTable(true)             // 自动建表
                .build();
    }

}
