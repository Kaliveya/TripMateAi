package com.sean.trip.tripmateai.external.amap;

import com.sean.trip.tripmateai.domain.dto.AmapWeatherResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AmapWeatherClient {

    private final RestClient restClient;

    @Value("${amap.api.key}")
    private String apiKey;

    public AmapWeatherClient(@Value("${amap.api.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public AmapWeatherResponse getForecast(String adcode) {
        return restClient.get()
                .uri("/v3/weather/weatherInfo?key={key}&city={city}&extensions=all",
                        apiKey, adcode)
                .retrieve()
                .body(AmapWeatherResponse.class);
    }
}