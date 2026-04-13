package com.sean.trip.tripmateai.external.amap;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class CityAdcodeMapper {
    // name -> adcode映射
    private final Map<String, String> nameToAdcode = new HashMap<>();

    // 启动时自动执行
    @PostConstruct
    public void load() throws IOException {
        var resource = new ClassPathResource("data/AMap_adcodes.csv");
        try (var reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

            reader.lines()
                    .skip(1)
                    .map(line -> line.split(","))
                    .filter(parts -> parts.length == 2)
                    .forEach(parts -> nameToAdcode.put(parts[0].trim(), parts[1].trim()));
        }
    }
    public String getAdcode(String cityName) {
        if (cityName == null) return null;

        // 1. 精确匹配（优先）："北京市" -> "110000"
        if (nameToAdcode.containsKey(cityName)) {
            return nameToAdcode.get(cityName);
        }

        // 2. 补全匹配："北京" -> 尝试 "北京市"
        String withSuffix = cityName + "市";
        if (nameToAdcode.containsKey(withSuffix)) {
            return nameToAdcode.get(withSuffix);
        }

        // 3. 模糊匹配（兜底）："北京朝阳" -> 找包含关键词的第一个
        return nameToAdcode.entrySet().stream()
                .filter(e -> e.getKey().contains(cityName) || cityName.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }
}
