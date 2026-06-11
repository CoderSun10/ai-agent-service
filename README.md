# AI 多 Agent 智能客服系统

> 基于 Spring Boot 3.3 + Java 21 + LangChain4j 的多 Agent 协作智能客服系统。
> 功能完整、架构清晰、可直接用于面试讲解。

## ✨ 功能特性

- **多轮自然语言对话**：Redis 维护对话短期记忆，滑动窗口控制上下文规模
- **RAG 知识库问答**：pgvector + HNSW 余弦检索，减少幻觉
- **Tool Calling 业务操作**：订单查询 / 物流追踪 / 退款 / 工单，Agent 自主调用
- **多 Agent 编排**：意图识别 → 知识检索 → 回复生成，按意图按需调度
- **流式输出（SSE）**：打字机效果，基于 Java 21 虚拟线程
- **低置信自动转人工**：Outbox 模式 + RabbitMQ，保证消息不丢失

## 🏗️ 技术栈

| 层次 | 技术 |
|------|------|
| 语言 / 框架 | Java 21、Spring Boot 3.3 |
| AI 框架 | LangChain4j 0.36（OpenAI 兼容协议接入 DeepSeek） |
| 向量库 | PostgreSQL 16 + pgvector（HNSW 索引） |
| 业务库 | MySQL 8 + MyBatis-Plus |
| 缓存 | Redis 7.2 |
| 消息队列 | RabbitMQ 3.13（含死信队列） |
| 鉴权 | Sa-Token |
| Embedding | all-MiniLM-L6-v2（本地 ONNX，384 维，离线可用） |

> 说明：DeepSeek 暂未提供 embedding 接口，知识库向量化采用本地内置模型，无需联网；
> 如需更高质量可替换为云端 embedding 服务（同时调整 `agent.embedding.dimension` 与建表维度）。

## 🚀 快速启动

### 1. 准备环境变量

```bash
cp .env.example .env
# 编辑 .env，填入 DeepSeek API Key
```

### 2. 一键启动（Docker Compose）

```bash
docker compose up -d --build
```

将启动 MySQL / PostgreSQL(pgvector) / Redis / RabbitMQ / 应用，建表脚本自动执行。

- 应用：http://localhost:8080
- RabbitMQ 控制台：http://localhost:15672 （guest/guest）

### 3. 本地开发启动

确保本地中间件已就绪（或仅用 compose 启动中间件），然后：

```bash
mvn spring-boot:run
```

默认 profile 为 `dev`，连接 localhost 中间件。

## 📡 接口一览

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 登录获取 token |
| POST | `/api/auth/logout` | 登出 |
| POST | `/api/chat/conversation` | 新建会话 |
| GET  | `/api/chat/stream` | **SSE 流式对话** |
| GET  | `/api/chat/history/{convId}` | 会话历史 |
| GET  | `/api/chat/conversations` | 会话列表 |
| POST | `/api/knowledge/upload` | 上传文档并索引 |
| GET  | `/api/knowledge/list` | 文档列表 |

> 完整联调脚本见 [`src/test/http/chat.http`](src/test/http/chat.http)。
> 示例账号：`demo / 123456`（见 `sample-data.sql`）。

### SSE 事件类型

| event | 含义 |
|-------|------|
| `conversation` | 返回会话 ID |
| `intent` | 意图识别结果 |
| `message` | 回复文本增量 token |
| `escalate` | 已转人工提示 |
| `done` | 结束标记 `[DONE]` |
| `error` | 异常信息 |

## 🧱 模块结构

```
com.agent
├── config         # LangChain4j / Rabbit / SaToken / Redis / MyBatisPlus / 线程池 / Web
├── common         # Result / 枚举 / 常量 / 异常 / 工具
├── controller     # Auth / Chat(SSE) / Knowledge
├── orchestrator   # AgentOrchestrator 多 Agent 编排核心
├── agent          # IntentAgent / RetrievalAgent / ReplyAgent
├── tool           # OrderQuery / Logistics / Refund / WorkOrder（@Tool）
├── rag            # DocumentLoader / VectorStoreService
├── memory         # RedisConversationMemory（滑动窗口）
├── escalation     # EscalationService(Outbox) / EscalationConsumer
├── service        # Auth / Conversation / Message / Order / Logistics / Refund / Knowledge
├── entity·mapper  # MyBatis-Plus 持久层
└── dto·vo         # 请求 / 响应对象
```

## 🧠 设计要点（面试讲解）

- **多 Agent 协作**：编排层自实现，按意图识别结果决定是否走 RAG，避免无谓 LLM 调用。
- **RAG 召回**：按段落语义递归分块（非定长截断），HNSW + 余弦距离 Top-5。
- **Outbox 模式**：会话状态更新与待发消息同事务落库，定时任务异步投递，解决「DB 成功但 MQ 失败」的消息丢失。
- **Token 滑动窗口**：Redis List 存历史，按「条数 + Token」双阈值裁剪最旧问答。
- **虚拟线程**：SSE 长连接用 Java 21 虚拟线程承载，避免占满 Tomcat 工作线程。
- **DeepSeek 接入**：完全兼容 OpenAI 协议，仅改 base-url / api-key。

## 🧪 测试

```bash
mvn test
```

包含 `TokenCounter`、`IntentType` 等纯逻辑单元测试（不依赖中间件）。

## 📅 开发迭代

| 阶段 | 目标 |
|------|------|
| 一 | 环境搭建 + 基础对话 + SSE |
| 二 | RAG 知识库 |
| 三 | 多 Agent 架构 |
| 四 | Tool Calling |
| 五 | 转人工 + 收尾 |
