package com.sean.trip.tripmateai.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.sean.trip.tripmateai.agent.TripPlannerAgent;
import com.sean.trip.tripmateai.domain.dto.GetTripPlanRequest;
import com.sean.trip.tripmateai.domain.vo.TripPlanVo;
import com.sean.trip.tripmateai.common.CommonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/tripPlan")
public class ChatController {

    @Autowired
    private TripPlannerAgent tripPlannerAgent;

    @GetMapping("/createSession")
    public CommonResponse createSession() {
        String sessionId = UUID.randomUUID().toString();
        return CommonResponse.success(sessionId);
    }

    @PostMapping("/chat")
    public CommonResponse tripPlan(@RequestBody GetTripPlanRequest request) {
        String response = tripPlannerAgent.chat(request.getSessionId(),request.getMessage());
        return CommonResponse.success(response);
    }
}