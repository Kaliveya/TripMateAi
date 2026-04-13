package com.sean.trip.tripmateai.domain.vo;

import lombok.Data;

import java.util.List;

@Data
public class TripPlanVo {

    private List<DayPlan> days;

    @Data
    public static class DayPlan {
        private int day;
        private List<PlanItem> plan;
    }

    @Data
    public static class PlanItem {
        private String time;
        private String activity;
        private String location;
        private String cost;
    }
}
