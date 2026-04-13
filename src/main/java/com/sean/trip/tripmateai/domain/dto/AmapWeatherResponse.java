package com.sean.trip.tripmateai.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class AmapWeatherResponse {
    private String status;
    private String count;
    private String info;
    private String infocode;

    private List<ForecastDTO> forecasts;

    @Data
    public static class ForecastDTO {
        private String city;
        private String adcode;
        private String province;
        private String reporttime;

        private List<CastDTO> casts;
    }

    @Data
    public static class CastDTO {
        private String date;
        private String week;

        private String dayweather;
        private String nightweather;

        private String daytemp;
        private String nighttemp;

        private String daywind;
        private String nightwind;

        private String daypower;
        private String nightpower;

        private String daytempFloat;
        private String nighttempFloat;

        public String toDescription() {
            String weekDay = switch (week) {
                case "1" -> "周一"; case "2" -> "周二"; case "3" -> "周三";
                case "4" -> "周四"; case "5" -> "周五"; case "6" -> "周六";
                default -> "周日";
            };
            return String.format("%s（%s）%s转%s，%s~%s°C，%s风%s级",
                    date, weekDay,
                    dayweather, nightweather,
                    nighttemp, daytemp,
                    daywind, daypower);
        }
    }
}