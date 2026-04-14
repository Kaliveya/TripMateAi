# TripMateAI

自我学习项目 — 智能旅游助手，基于 AI 大模型的旅行规划服务。

## 技术栈

| 类别 | 技术                                                                                                                        |
|------|---------------------------------------------------------------------------------------------------------------------------|
| 后端框架 | Spring Boot 3.5.13 + Java 17                                                                                              |
| ORM | MyBatis Plus 3.5.9                                                                                                        |
| AI 框架 | LangChain4j 1.9.1                                                                                                         |
| 大语言模型 | MiniMax M2.7 <br/>官网/注册地址：https://www.minimaxi.com<br/>开发者控制台：https://platform.minimaxi.com/user-center/basic-information |
| 向量数据库 | PGVector (PostgreSQL)                                                                                                     |
| Embedding | BGE 中文小模型（512 维）                                                                                                          |
| 地图服务 | 高德地图 MCP<br/>官网/注册地址：https://lbs.amap.com<br/>开发者控制台：https://console.amap.com/dev/key/app                                 |
| 数据导入 | LangChain4j Document Loader                                                                                               |

## 功能特性

| 功能 | 描述 |
|------|------|
| 创建会话 | 生成唯一 Session ID，用于追踪对话上下文 |
| AI 对话 | 智能对话，支持 Tool Calling，多轮对话记忆 |
| 天气查询 | 查询目的地天气预报 |
| 景点推荐 | 查询热门景点信息（开放时间、门票、简介）|
| 美食推荐 | 推荐特色美食和餐厅 |
| 预算估算 | 根据天数、人数、消费等级估算旅行预算 |
| 地图搜索 | 高德 MCP 提供地点搜索、路线规划 |
| 导航生成 | 自动生成高德地图导航链接 |
| 行程地图 | 生成一张图览全览的个人地图 |
| 知识导入 | 导入 Markdown 格式旅游知识到向量库 |

## 快速开始

### 1. 环境要求

- Java 17+
- PostgreSQL 15+（需开启 pgvector 扩展）
- Maven 3.6+

### 2. 数据库初始化

```sql
-- 创建数据库
CREATE DATABASE "TripMate";

-- 创建会话记忆表
CREATE TABLE "public"."chat_memory" (
  "id"         int8         NOT NULL DEFAULT nextval('chat_memory_id_seq'::regclass),
  "session_id" varchar(255) NOT NULL,
  "content"    text         NOT NULL,
  "created_at" timestamp(6)          DEFAULT now(),
  "updated_at" timestamp(6)          DEFAULT now(),
  CONSTRAINT "chat_memory_pkey"           PRIMARY KEY ("id"),
  CONSTRAINT "chat_memory_session_id_key" UNIQUE ("session_id")
);

-- 向量检索扩展（PGVector）
CREATE EXTENSION IF NOT EXISTS vector;

-- 向量知识库表由 LangChain4j 自动创建
-- 表名：trip_knowledge，维度：512
```

### 3. 配置环境变量

```bash
# MiniMax API（必填）
export MINIMAX_API_KEY="your-api-key"
export MINIMAX_BASE_URL="https://api.minimaxi.com/v1/"
export MINIMAX_MODEL_NAME="MiniMax-M2.7"

# PostgreSQL（必填）
export DB_URL="jdbc:postgresql://localhost:5432/TripMate"
export DB_USERNAME="sean"
export DB_PASSWORD="your-password"

# 高德地图 API（必填）
export AMAP_API_KEY="your-amap-key"

# 可选
export SERVER_PORT=8666
```

### 4. 编译运行

```bash
# 编译
./mvnw clean package -DskipTests

# 运行
./mvnw spring-boot:run
```

### 5. API 调用

```bash
# 创建会话
curl "http://localhost:8666/tripPlan/createSession"

# AI 对话
curl -X POST "http://localhost:8666/tripPlan/chat" \
  -H "Content-Type: application/json" \
  -d '{"sessionId": "550e8400-e29b-41d4-a716-446655440000", "message": "我想去上海玩3天，请帮我规划一下"}'

# 导入旅游知识（可选）
curl -X POST "http://localhost:8666/dataImport/importMdData" \
  -H "Content-Type: application/json" \
  -d '{"city": "上海", "mdFilePath": "/path/to/shanghai.md"}'
```

## API 接口

### 会话管理

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/tripPlan/createSession` | 创建新会话，返回 Session ID |

### AI 对话

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/tripPlan/chat` | 发送对话消息，获取 AI 回复 |

### 知识导入

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/dataImport/importMdData` | 导入 Markdown 到向量库 |

## 请求 / 响应格式
#### 1. // POST /tripPlan/chat
```json
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "message": "去杭州西湖玩2天，求行程安排"
}
```

```json
// POST /dataImport/importMdData
{
  "city": "杭州",
  "mdFilePath": "/data/knowledge/hangzhou.md"
}
```

```json
// 统一响应格式
{
  "code": 200,
  "message": "success",
  "data": "这是为您安排的杭州西湖2日游行程..."
}
```

## 系统架构

```
┌──────────────────────────────────────────────────┐
│                     Client                       │
│                  HTTP 请求 / 响应                  │
└──────────────────────┬───────────────────────────┘
                       │
┌──────────────────────▼───────────────────────────┐
│         Spring Boot 服务层（端口 8666）             │
│                                                  │
│  ┌─────────────────┐  ┌──────────────────────┐   │
│  │ ChatController  │  │ DataImportController  │   │
│  │ createSession   │  │  Markdown 知识导入     │   │
│  │ chat            │  │  向量化存储            │   │
│  └────────┬────────┘  └──────────────────────┘   │
│           │                                      │
│  ┌────────▼──────────────────────────────────┐   │
│  │           LangChain4j Agent               │   │
│  │       意图识别 / 推理 / Tool Calling        │   │
│  └────────┬──────────────────────────────────┘   │
└───────────┼──────────────────────────────────────┘
            │
┌───────────▼──────────────────────────────────────┐
│                    工具层                         │
│                                                  │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐        │
│  │ 天气查询  │  │ 景点/美食 │  │ 地图/路线 │  ...   │
│  │ 高德 API │  │ RAG 检索  │  │ 高德 MCP │        │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘        │
└───────┼─────────────┼─────────────┼──────────────┘
        │             │             │
┌───────▼──────┐ ┌────▼──────┐ ┌───▼──────────────┐
│  MiniMax API │ │  PGVector │ │   高德地图 MCP    │
│  大语言模型   │ │  向量知识库 │ │  地点搜索 / 导航  │
│  推理服务    │ │  会话记忆  │ │  链接生成         │
└──────────────┘ └───────────┘ └──────────────────┘
```

## 工作流程

```
用户输入 → 加载会话记忆 → Agent 推理 → 并行 Tool 调用 → 整合结果 → 存储记忆 → 返回响应
```

1. 用户发送消息，附带 `session_id`
2. 从 PostgreSQL 加载会话历史
3. Agent 分析意图，选择并调用相应工具
4. **第一阶段**：并行查询天气、景点、美食、预算
5. **第二阶段**：对推荐结果调用高德地图搜索
6. **第三阶段**：生成个人地图链接
7. **第四阶段**：生成各景点导航链接
8. 整合结果，生成结构化行程，存储对话记忆并返回

> 详细 Prompt 见 `src/main/resources/prompts/trip-planner-system.txt`

## 项目结构

```
src/main/java/com/sean/trip/tripmateai/
├── TripMateAiApplication.java
├── agent/
│   └── TripPlannerAgent.java          # AI Agent 定义
├── config/
│   ├── AmapMcpConfig.java             # 高德 MCP 配置
│   ├── EmbeddingConfig.java           # 向量存储配置
│   └── TripPlannerConfig.java         # Agent 配置
├── controller/
│   ├── ChatController.java            # 对话接口
│   └── DataImportController.java      # 数据导入接口
├── external/amap/
│   ├── AmapWeatherClient.java         # 天气 API
│   └── CityAdcodeMapper.java          # 城市编码映射
├── mapper/
│   └── ChatMemoryMapper.java          # MyBatis 映射
├── service/impl/
│   └── DataImportServiceImpl.java
├── store/
│   └── PostgresChatMemoryStore.java   # 对话记忆存储
├── tool/
│   └── TravelTools.java               # 本地工具集
└── domain/
    ├── dto/                           # 请求 / 响应 DTO
    ├── entity/                        # 数据库实体
    └── vo/                            # 视图对象
```

## 开发者

Sean

## License

MIT