---
title: Frequently Asked Questions
keywords: [Spring AI Alibaba, FAQ]
description: "A compilation of common issues and solution guides for using Spring AI Alibaba."
---

## `spring-ai` dependency download fails during Maven build
In version 1.0.0 and earlier, since the official Spring AI packages had not yet been published to the central repository and were instead published to Spring's own maintained repository, the following configuration is required:

```xml
```

If you still get errors after adding the above configuration, check whether a mirror proxy is configured in `~/.m2/settings.xml`. If a mirror proxy exists, add a configuration similar to the following:

```xml
<!-- ~/.m2/settings.xml -->

```

## How to determine the compatibility between Spring AI Alibaba, Spring AI, and Spring Boot versions
Spring AI Alibaba uses a four-digit versioning scheme. The first three digits correspond to the main version of Spring AI, and the Spring AI Alibaba community continuously iterates the fourth digit on top of the first three main version digits.

Below are some version compatibility mappings; new releases follow the same pattern:

| Spring AI Alibaba | Spring AI | Spring Boot |
| --- | --- | --- |
| 1.0.0.2 | 1.0.0 | 3.4.5 |
| 1.0.0-M6.1 | 1.0.0-M6 | 3.4.2 |

## What are the differences between Spring AI and Spring AI Alibaba?
Spring AI is positioned as a low-level AI application development framework, providing foundational atomic abstractions needed for AI development, including model adapters, tool definitions, vector database access, etc. Spring AI Alibaba is positioned as an AI agent development framework, providing a Graph framework based on graph algorithms for agent programming, making it easier for developers to build workflow and multi-agent applications. To help understand, here's an imperfect analogy: if Spring AI is the LangChain framework in the LangChain ecosystem, then Spring AI Alibaba is the LangGraph framework in that same ecosystem.

Beyond the framework itself, Spring AI Alibaba represents Alibaba Cloud's enterprise-grade best practices and complete solution for agent development based on the Spring AI framework, deeply integrated with the Alibaba open-source ecosystem and Alibaba Cloud platform services, including:
* Integration with Bailian Dashscope model services, supporting mainstream model series such as Qwen, Deepseek, etc.
* Integration with the Bailian AgentScope intelligent agent application platform, providing bidirectional conversion between low-code and high-code, improving R&D efficiency
* Integration with Bailian ChatBI, providing an open-source framework and service for automatic natural language to SQL generation
* Integration with Alibaba Cloud products, including AnalyticDB vector retrieval, OpenSearch vector retrieval, IQS information retrieval service, etc.
* Integration with the open-source Nacos and Higress ecosystem, providing MCP registry, MCP intelligent routing, Prompt management, model proxy, and other capabilities
* Providing cutting-edge agent product implementations and complete solutions, including JManus, DeepResearch, NL2SQL, etc.
* Providing a complete supporting ecosystem for AI application development, including local development tools, project building platforms, etc.


## Is there a comparison of mainstream Java AI frameworks?

Below is a comparison of current mainstream Java AI frameworks.

| **Comparison Dimension** | **Spring AI Alibaba** | **Spring AI** | **LangChain4J** |
| --- | --- | --- | --- |
| **Spring Boot Integration** | Native support | Native support | Community adaptation |
| **Text Models** | Mainstream models, extensible | Mainstream models, extensible | Mainstream models, extensible |
| **Audio/Video, Multimodal, Vector Models** | Supported | Supported | Supported |
| **RAG** | Modular RAG | Modular RAG | Modular RAG |
| **Vector Databases** | Mainstream vector databases, Alibaba Cloud ADB, OpenSearch, etc. | Mainstream vector databases | Mainstream vector databases |
| **MCP Support** | Supported, Nacos MCP Registry support | Supported | Supported |
| **Function Calling** | Supported (20+ official tool integrations) | Supported | Supported |
| **Prompt Templates** | Hardcoded, no declarative annotations | Hardcoded, no declarative annotations | Declarative annotations |
| **Prompt Management** | Nacos configuration center | None | None |
| **Chat Memory** | Optimized JDBC, Redis, ElasticSearch | JDBC, Neo4j, Cassandra | Multiple implementation adapters |
| **Observability** | Supported, can connect to Alibaba Cloud ARMS | Supported | Partially supported |
| **Workflow** | Supported, compatible with Dify and Bailian DSL | None | None |
| **Multi-agent** | Supported, official general-purpose agent implementations | None | None |
| **Model Evaluation** | Supported | Supported | Supported |
| **Community Activity & Documentation** | Official community, highly active | Official community, highly active | Individually initiated community |
| **Developer Productivity Components** | Rich, including debugging, code generation tools, etc. | None | None |
| **Example Repositories** | Rich, highly active | Few | Rich, highly active |
