package com.sean.trip.tripmateai.tool;

import com.sean.trip.tripmateai.domain.dto.AmapWeatherResponse;
import com.sean.trip.tripmateai.external.amap.AmapWeatherClient;
import com.sean.trip.tripmateai.external.amap.CityAdcodeMapper;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;

import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TravelTools {

    @Autowired
    private AmapWeatherClient amapWeatherClient;
    @Autowired
    private CityAdcodeMapper cityAdcodeMapper;
    @Autowired
    private EmbeddingModel embeddingModel;
    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    @Tool("查询目的地天气预报")
    public String getWeather(String city) {
        // 根据城市名获取adcode
        String adcode = cityAdcodeMapper.getAdcode(city);
        if (adcode == null) {
            return "暂不支持查询该城市的天气";
        }
        try {
            AmapWeatherResponse response = amapWeatherClient.getForecast(adcode);
            if (!"1".equals(response.getStatus()) || response.getForecasts().isEmpty()) {
                return "天气数据获取失败，请稍后重试。";
            }
            var forecast = response.getForecasts().get(0);
            var sb = new StringBuilder();
            sb.append(forecast.getProvince()).append(forecast.getCity()).append(" 天气预报\n");
            sb.append("更新时间：").append(forecast.getReporttime()).append("\n\n");
            forecast.getCasts().forEach(cast ->
                    sb.append("  ").append(cast.toDescription()).append("\n"));
            return sb.toString();
        } catch (Exception e) {
            return "天气服务暂时不可用，建议用户自行查询" + city + "当地天气。";
        }
    }

    // ========== 景点 → RAG ==========
    @Tool("查询目的地的热门景点，包含景点名称、开放时间、门票价格、简介")
    public String getAttractions(String city, String preference) {
        String query = city + " 景点 " + (preference != null ? preference : "");
        return ragSearch(query, city);
    }

    // ========== 美食 → RAG ==========
    @Tool("推荐目的地的特色美食和餐厅，包含价格区间")
    public String getFoodRecommendations(String city, String budget) {
        String query = city + " 美食 " + (budget != null ? budget : "");
        return ragSearch(query, city);
    }

    // ========== 预算 → RAG ==========
    @Tool("根据旅行天数、人数、消费等级估算旅行总预算，返回明细分项")
    public String estimateBudget(String city, int days, int people, String level) {
        String query = city + " 住宿 消费 价格 预算 " + level;
        String ragResult = ragSearch(query, city);

        // RAG 找到参考数据后，再拼一个结构化预算
        int hotelPerNight = level.equals("经济") ? 200 : level.equals("舒适") ? 500 : 1200;
        int dailyExpense  = level.equals("经济") ? 150 : level.equals("舒适") ? 300 : 600;
        int total = (hotelPerNight * days + dailyExpense * days) * people;

        return String.format("""
                %d人 %d天 %s级别预算估算：
                住宿：%d元/晚 × %d晚 × %d人 = %d元
                餐饮+景点+交通：%d元/天 × %d天 × %d人 = %d元
                建议总预算：约 %d 元
                
                参考资料：
                %s
                """,
                people, days, level,
                hotelPerNight, days, people, hotelPerNight * days * people,
                dailyExpense, days, people, dailyExpense * days * people,
                total,
                ragResult);
    }

    // ========== 核心RAG检索方法 ==========

    /**
     * @param query 检索语句
     * @param city  城市过滤，null则不过滤
     */
    private String ragSearch(String query, String city) {
        // 1. query向量化
        Embedding queryEmbedding = embeddingModel.embed(query).content();

        // 2. 构建过滤条件
        Filter filter = buildFilter(normalizeCity(city));

        // 3. 向量检索
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(5)
                .minScore(0.6)
                .filter(filter)
                .build();

        List<EmbeddingMatch<TextSegment>> matches =
                embeddingStore.search(request).matches();

        // 4. 没找到兜底
        if (matches.isEmpty()) {
            return "暂无 " + city + " 的相关资料，建议用户自行查询。";
        }

        // 5. 拼装结果
        return matches.stream()
                .map(m -> m.embedded().text())
                .collect(Collectors.joining("\n---\n"));
    }

    private Filter buildFilter(String city) {
        if (city != null ) {
            return  metadataKey("city").isEqualTo(city);
        } else if (city != null) {
            return metadataKey("city").isEqualTo(city);
        }
        return null;
    }

    private String normalizeCity(String city) {
        if (city == null) return null;
        return city.replaceAll("(省|市|区|县|自治区|特别行政区)$", "").trim();
    }
}