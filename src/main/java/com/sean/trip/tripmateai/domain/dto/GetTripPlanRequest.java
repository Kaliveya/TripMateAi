package com.sean.trip.tripmateai.domain.dto;

import lombok.Data;

@Data
public class GetTripPlanRequest {

    private String sessionId;

    private String message;
}
