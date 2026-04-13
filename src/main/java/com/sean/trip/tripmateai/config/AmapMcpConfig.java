package com.sean.trip.tripmateai.config;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import dev.langchain4j.service.tool.ToolProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;
import java.util.logging.Logger;


@Configuration
public class AmapMcpConfig {

    @Value("${amap.api.key}")
    private String amapKey;
    private static final Logger log = Logger.getLogger(AmapMcpConfig.class.getName());

    @Bean
    public McpClient amapMcpClient() {
        // 高德 Streamable HTTP 端点
        String mcpUrl = "https://mcp.amap.com/mcp?key=" + amapKey;
        McpTransport transport = new StreamableHttpMcpTransport.Builder()
                .url(mcpUrl)
                .timeout(Duration.ofSeconds(30))
                .logRequests(true)
                .logResponses(true)
                .build();

        return new DefaultMcpClient.Builder()
                .transport(transport)
                .build();
    }

    @Bean
    public ToolProvider amapMcpToolProvider(McpClient amapMcpClient) {
        // 验证能否拿到工具列表
        amapMcpClient.listTools().forEach(t ->
                log.info("MCP Tool registered: " + t.name())
        );
        // 写入工具列表
        return McpToolProvider.builder()
                .mcpClients(List.of(amapMcpClient))
                .build();
    }
}