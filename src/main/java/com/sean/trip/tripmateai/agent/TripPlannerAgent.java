package com.sean.trip.tripmateai.agent;

import com.sean.trip.tripmateai.domain.vo.TripPlanVo;
import com.sean.trip.tripmateai.tool.TravelTools;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import org.springframework.stereotype.Component;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

/**
 * 旅游助手Agent - 支持Tool Calling
 */
@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT,
        chatModel = "openAiChatModel",//兼容openAI的模型
        chatMemoryProvider = "chatMemoryProvider",  //注入数据库
        tools = {"travelTools"},              // 注入本地Tool
        toolProvider = "amapMcpToolProvider"  // 注入高德MCP
)
public interface TripPlannerAgent {
    @SystemMessage(fromResource = "prompts/trip-planner-system.txt")
    String chat(@MemoryId String sessionId, @UserMessage String message);
}