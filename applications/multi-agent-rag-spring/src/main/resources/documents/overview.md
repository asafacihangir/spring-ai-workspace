---
title: Spring AI Alibaba Overview
keywords: [What is Spring AI Alibaba, Spring AI Alibaba Introduction, Spring AI Introduction]
description: "Spring AI Alibaba is an AI framework built on Spring AI, deeply integrated with the Bailian platform, supporting ChatBot, workflow, and multi-agent application development patterns."
---
## What is Spring AI Alibaba
Spring AI Alibaba (SAA) is an AI framework built on Spring AI, deeply integrated with the Bailian platform, supporting ChatBot, workflow, and multi-agent application development patterns.

![spring ai alibaba architecture.png](/img/user/ai/overview/1.0.0/spring-ai-alibaba-architecture.png)

In the 1.0 release, Spring AI Alibaba provides the following core capabilities, enabling developers to quickly build their own Agent, Workflow, or Multi-agent applications.

1. **Graph Multi-Agent Framework.** With Spring AI Alibaba Graph, developers can quickly build workflow and multi-agent applications without worrying about the underlying implementation of process orchestration, context memory management, etc. By combining Graph with low-code and self-planning agents, it provides developers with more flexible choices ranging from low-code, high-code to zero-code agent construction.
2. **Solving enterprise pain points in agent deployment through AI ecosystem integration.** Spring AI Alibaba supports deep integration with the Bailian platform, providing model access and RAG knowledge base solutions; supports seamless integration with observability products like ARMS and Langfuse; supports enterprise-grade MCP integration, including Nacos MCP Registry for distributed registration and discovery, automatic Router routing, etc.
3. **Exploring general-purpose agent products and platforms with autonomous planning capabilities.** The community has released the JManus agent implemented based on the Spring AI Alibaba framework. Beyond matching OpenManus's general agent capabilities, our goal is to explore the application of autonomous planning in agent development through JManus, providing developers with more flexible choices ranging from low-code, high-code to zero-code agent construction.

### Relationship and Differences with Spring AI
Spring AI is an open-source framework maintained by the official Spring community, initially releasing its first Milestone version in May 2024, and officially releasing the first 1.0 GA version in May 2025. Spring AI focuses on low-level atomic capability abstractions for AI development and seamless integration with the Spring Boot ecosystem, such as model communication (ChatModel), prompts (Prompt), Retrieval-Augmented Generation (RAG), memory (ChatMemory), tools (Tool), Model Context Protocol (MCP), etc., helping Java developers quickly build AI applications.

Since its official open-source release in September 2024, Spring AI Alibaba has maintained deep communication and collaboration with the Spring AI community, releasing multiple Milestone versions and establishing deep partnerships with many enterprise clients. During these exchanges, we've seen the advantages and limitations of low-code development patterns, customers' needs evolving from chatbots and single agents to multi-agent architecture solutions as business complexity increases, and the difficulties encountered in taking agent development from simple demos to production deployment.

## Quick Start
### Developing Your First Spring AI Alibaba Application
Add the following dependencies to your Spring Boot project to begin your AI agent development journey.

```xml
	<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>com.alibaba.cloud.ai</groupId>
				<artifactId>spring-ai-alibaba-bom</artifactId>
				<version>1.0.0.2</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>

<dependencies>
  <dependency>
    <groupId>com.alibaba.cloud.ai</groupId>
    <artifactId>spring-ai-alibaba-starter-dashscope</artifactId>
  </dependency>
</dependencies>
```

You can refer to the [Quick Start](./get-started/chatbot/) published on our official website to learn how to develop Chatbot, agent, or workflow applications. In general, depending on the scenario, you can choose to use either `ChatClient` or `Spring AI Alibaba Graph` as the two core components for developing AI applications.

### Try the Official Playground Example
The Spring AI Alibaba official community has developed a **complete agent Playground example with both "Frontend UI + Backend Implementation"**. The example is built using Spring AI Alibaba and lets you experience all core framework capabilities including chatbot, multi-turn conversation, image generation, multimodal, tool calling, MCP integration, RAG knowledge base, etc.

The overall interface after running looks like this:

![spring ai alibaba playground.png](/img/user/ai/overview/1.0.0/playground.png)

You can [deploy the Playground example locally](https://github.com/springaialibaba/spring-ai-alibaba-examples/tree/main/spring-ai-alibaba-playground) and access it through your browser, or copy the source code and adjust it to your business needs to quickly build your own AI application.



For more examples, if you want to learn more Spring AI Alibaba framework usage through source code examples, please refer to our official example repository:

[https://github.com/springaialibaba/spring-ai-alibaba-examples](https://github.com/springaialibaba/spring-ai-alibaba-examples)

## Start Your Spring AI Alibaba 1.0 Journey
### Supporting All Spring AI Core Features
Spring AI Alibaba is built on Spring AI, therefore Spring AI Alibaba inherits all of Spring AI's atomic capability abstractions, and on top of that, enriches and expands the adaptation of core components including models, vector stores, memory, RAG, etc., enabling integration with Alibaba Cloud's AI ecosystem.

Regarding the Spring AI 1.0 GA version, the official Spring AI blog provides detailed explanations, including the framework's core design philosophy and specific feature usage. Below is our interpretation based on the official blog, feel free to review as needed:

+ Spring AI Core Feature Details
    - [Prompt](https://java2ai.com/blog/spring-ai-100-ga-released/?spm=5176.29160081.0.0.2856aa5c2PwbQU#%E6%8F%90%E7%A4%BAprompt)
    - [The Augmented LLM](https://java2ai.com/blog/spring-ai-100-ga-released/?spm=5176.29160081.0.0.2856aa5c2PwbQU#%E6%A8%A1%E5%9E%8B%E5%A2%9E%E5%BC%BAthe-augmented-llm)
    - [Advisors](https://java2ai.com/blog/spring-ai-100-ga-released/?spm=5176.29160081.0.0.2856aa5c2PwbQU#%E9%A1%BE%E9%97%AEadvisors)
    - [Retrieval](https://java2ai.com/blog/spring-ai-100-ga-released/?spm=5176.29160081.0.0.2856aa5c2PwbQU#%E6%A3%80%E7%B4%A2retrieval)
    - [ChatMemory](https://java2ai.com/blog/spring-ai-100-ga-released/?spm=5176.29160081.0.0.2856aa5c2PwbQU#%E8%AE%B0%E5%BF%86chatmemory)
    - [Tool](https://java2ai.com/blog/spring-ai-100-ga-released/?spm=5176.29160081.0.0.2856aa5c2PwbQU#%E5%B7%A5%E5%85%B7tool)
    - [Evaluation](https://java2ai.com/blog/spring-ai-100-ga-released/?spm=5176.29160081.0.0.2856aa5c2PwbQU#%E8%AF%84%E4%BC%B0evaluation)
    - [Observability](https://java2ai.com/blog/spring-ai-100-ga-released/?spm=5176.29160081.0.0.2856aa5c2PwbQU#%E5%8F%AF%E8%A7%82%E6%B5%8B%E6%80%A7observability)
    - [Model Context Protocol (MCP)](https://java2ai.com/blog/spring-ai-100-ga-released/?spm=5176.29160081.0.0.2856aa5c2PwbQU#%E6%A8%A1%E5%9E%8B%E4%B8%8A%E4%B8%8B%E6%96%87%E5%8D%8F%E8%AE%AEmcp)
        * [MCP Client](https://java2ai.com/blog/spring-ai-100-ga-released/?spm=5176.29160081.0.0.2856aa5c2PwbQU#mcp-%E5%AE%A2%E6%88%B7%E7%AB%AF)
        * [MCP Server](https://java2ai.com/blog/spring-ai-100-ga-released/?spm=5176.29160081.0.0.2856aa5c2PwbQU#mcp-%E6%9C%8D%E5%8A%A1%E5%99%A8)
    - [MCP and Security](https://java2ai.com/blog/spring-ai-100-ga-released/?spm=5176.29160081.0.0.2856aa5c2PwbQU#mcp-%E5%92%8C%E5%AE%89%E5%85%A8)

For Spring AI Alibaba's adaptation with the Alibaba Cloud AI ecosystem, please refer to the official documentation.

### Spring AI Alibaba Graph Multi-Agent Framework
Spring AI Alibaba Graph is one of the core community implementations and represents where the framework's design philosophy differs from Spring AI's focus on only low-level atomic abstractions. Spring AI Alibaba aims to help developers more easily build agent applications. With Graph, developers can build workflow and multi-agent applications. Spring AI Alibaba Graph's design philosophy draws inspiration from LangGraph, so it can be understood to some extent as a Java implementation of LangGraph. The community has added a large number of pre-built Nodes, simplified the State definition process, etc., making it easier for developers to write workflows, multi-agents, and more comparable to low-code platforms.



Spring AI Alibaba Graph core capabilities:

+ Supports Multi-agent, with built-in ReAct Agent, Supervisor, and other common agent patterns
+ Supports workflows with built-in workflow nodes, aligned with mainstream low-code platforms
+ Native Streaming support
+ Human-in-the-loop, through human confirmation nodes, supporting state modification and execution resumption
+ Supports memory and persistent storage
+ Supports process snapshots
+ Supports nested branches and parallel branches
+ PlantUML, Mermaid visualization export



For specific usage of Graph, please follow the official documentation updates. In the following sections, we will introduce the general-purpose agent platforms officially released based on Spring AI Alibaba — you can consider these official agent implementations as best practice applications of Graph.

### Enterprise-Level AI Application Ecosystem Integration
During the production deployment of Agents, users need to address various issues including agent effectiveness evaluation, MCP tool integration, Prompt management, Token context, visual Tracing, etc. Spring AI Alibaba provides comprehensive enterprise-grade production solutions for agents by deeply integrating with Nacos3, Higress AI Gateway, Alibaba Cloud ARMS, Alibaba Cloud vector retrieval databases, and the Bailian agent platform, accelerating the transition of agents from demo to production deployment.

![spring ai alibaba ecosystem.png](/img/user/ai/overview/1.0.0/spring-ai-alibaba-ecosystem.png)

1. **Enterprise-Level MCP Deployment and Proxy Solution**

Spring AI Alibaba MCP supports distributed deployment and load-balanced invocation of MCP Servers through integration with Nacos MCP Registry. For existing Spring Cloud, Dubbo, and other applications, it supports zero-code transformation for publishing APIs as MCP services. Developers can build their own MCP Server service proxies through Spring AI Alibaba MCP, supporting automatic loading of MCP metadata from the Nacos center.

2. **AI Gateway Integration to Improve Model Call Stability and Flexibility**

If you use Higress as a backend model proxy, you can connect to the Higress AI model proxy service through the standard OpenAI interface — just use `spring-ai-starter-model-openai`.

If you have existing API services that need to be used without code modification, you can use Higress as a proxy solution from API to MCP services.

3. **Reducing Enterprise Data Integration Costs and Improving AI Data Application Effectiveness**

**a. Bailian RAG Knowledge Base**

Bailian is a visual AI agent application development platform that provides RAG knowledge base management capabilities. Simply put, you can upload private data to the Bailian platform, leveraging its data parsing, chunking, and vectorization capabilities for data preprocessing. The processed data can then be used for subsequent Spring AI Alibaba agent application retrieval, taking advantage of Bailian's powerful data processing capabilities.

**b. Bailian ChatBI — Automatic Natural Language to SQL Generation**

The Spring AI Alibaba NL2SQL module uses LLM-based ChatBI technology to help users easily achieve natural language interactive data analysis. It understands the user's database schema and helps automatically generate SQL query statements. Whether it's simple conditional filtering or complex aggregation statistics and multi-table joins, it can accurately generate the corresponding SQL statements.

4. **Observability and Effectiveness Evaluation — Accelerating Agent Transition from Demo to Production**

Spring AI provides default SDK instrumentation at multiple key points to record metrics and tracing information during runtime, including model calls, vector retrieval, tool calls, and other critical stages. Spring AI tracing information is compatible with OpenTelemetry, so theoretically it can connect to mainstream open-source platforms like Langfuse, or Alibaba Cloud ARMS.

## From Chatbots and Workflows to Multi-Agents
### ChatBot
AI application development is more than just stateless LLM API calls. Due to the nature of LLM pre-training, AI applications also need capabilities for domain data retrieval (RAG), conversation memory (Memory), tool calling (Tool), etc. These external integrations are collectively called the Augmented LLM pattern, which allows developers to bring their own data and external APIs directly into the model's inference process.

<img src="/img/user/ai/overview/1.0.0/chatbot.png" alt="chatbot" style="max-width: 500px; height: auto;" />

> This diagram is from Anthropic's "Building Effective AI Agents" article

ChatClient is the most core component in Spring AI. Developers can use ChatClient to build their own chatbot or agent applications. ChatClient supports the Augmented LLM pattern, attaching external data and services like Retrieval, Tools, Memory to model calls.



```java
Flux<String> response = chatClient.prompt(query)
        .tools(toolCallbacks)
        .advisors(new QuestionAnswerAdvisor())
        .stream()
        .content();
```



We refer to AI applications developed with ChatClient as single-agent applications. This might be our most ideal agent development pattern — it's simple and straightforward: give the model all the tools, context information, etc., and let the model continuously make decisions and iterate until the task is completed. However, things are far from that simple. Model capabilities are still far from what we desire. When we give the model too much context and too many tools, overall effectiveness degrades, and sometimes things deviate significantly from our expectations. Therefore, we consider breaking down complex problems, and there are currently two commonly used patterns: **Workflow and Multi-agent**.

### Workflow
**Workflow** is a relatively fixed pattern for manually decomposing tasks, breaking a large task into a fixed process with multiple branches. The advantage of workflows is strong determinism — the model acts as a node in the process primarily for classification and decision-making, making it more suitable for application scenarios with strong categorical attributes like intent recognition. Workflows also have clear disadvantages: they require developers to have deep understanding of business processes, the entire flow is designed by humans, and the model mainly serves for content generation, summarization, and classification — not maximizing the model's reasoning capabilities. Many people criticize this pattern as not being intelligent enough.



Spring AI Alibaba Graph makes it easy to develop workflows by declaring different nodes and connecting them into a flowchart.


![spring ai alibaba workflow](/img/user/ai/overview/1.0.0/workflow.png)



It's worth noting that Spring AI Alibaba Graph provides a large number of pre-built nodes that align with mainstream low-code platforms such as Dify, Bailian, etc. Typical nodes include LlmNode (LLM node), QuestionClassifierNode (question classification node), ToolNode (tool node), etc., freeing users from repetitive development and definition, allowing them to focus solely on process orchestration.



The above is a visually designed "Customer Feedback Classification System" workflow. The corresponding Spring AI Alibaba Graph code is as follows:


```java
StateGraph stateGraph = new StateGraph("Consumer Service Workflow Demo", stateFactory)
			.addNode("feedback_classifier", node_async(feedbackClassifier))
			.addNode("specific_question_classifier", node_async(specificQuestionClassifier))
			.addNode("recorder", node_async(new RecordingNode()))

			.addEdge(START, "feedback_classifier")
			.addConditionalEdges("feedback_classifier",edge_async(new CustomerServiceController.FeedbackQuestionDispatcher()),Map.of("positive", "recorder", "negative", "specific_question_classifier"))
			.addConditionalEdges("specific_question_classifier",edge_async(new CustomerServiceController.SpecificQuestionDispatcher()),Map.of("after-sale", "recorder", "transportation", "recorder", "quality", "recorder", "others","recorder"))
			.addEdge("recorder", END);
```

### Multi-Agent
Another solution for complex task decomposition is **Multi-agent**. Compared to workflows, multi-agents also follow specific processes, but possess more autonomy and flexibility in the overall decision-making and execution flow. Multiple sub-agents collaborate through communication to complete task resolution. In the industry, there are several common multi-agent communication models. Below are some typical examples:

<img src="/img/user/ai/overview/1.0.0/multi-agent.png" alt="multi-agent" style="max-width: 500px; height: auto;" />

> Image from the official LangChain blog

Spring AI Alibaba Graph can be used to develop various multi-agent patterns. The official community has currently released several agent products developed based on Spring AI Alibaba Graph, including the general-purpose agent JManus, DeepResearch agent, AgentScope, etc.

## Building the Next Generation General-Purpose Agent Platform
Spring AI Alibaba is positioned as an agent framework centered on `ChatClient` and `Graph` abstractions, along with ecosystem integration around the framework, to help users quickly build enterprise-grade AI agents.

With the rapid development of general-purpose agent patterns, the community is also exploring agent products and platforms with autonomous planning capabilities based on Spring AI Alibaba. JManus and DeepResearch have already been released. Through agent products like JManus, we explore the limitless potential of agents in solving open-ended problems in daily life and work efficiency on one hand; on the other hand, the community continues to explore vertical domains such as agent development platforms and deep search, aiming to provide developers with a zero-code agent development experience based on natural language, beyond low-code platforms and high-code frameworks.

### JManus Agent Platform
When we first released JManus, it was positioned as a fully Java-centric, completely open-source reimplementation of OpenManus — a general-purpose AI Agent product implemented based on Spring AI Alibaba, including a well-designed frontend UI interaction interface.


As we deepened our exploration of general-purpose agents and other directions, we also adjusted JManus's positioning as a general-purpose agent end product. The emergence of Manus gave people limitless imagination about the capabilities of general-purpose agents for automatic planning and plan execution, excelling at solving open-ended problems with broad applications in daily life and work scenarios. However, in practice, people have also come to realize that within the current and foreseeable future model capabilities, relying entirely on the automatic planning mode of general-purpose agents makes it difficult to solve highly deterministic enterprise scenarios. The typical characteristic of enterprise business scenarios is determinism — we need customized tools and sub-agents, stable and deterministic planning and processes. Therefore, we hope JManus can become an agent development platform, allowing users to build their own vertical domain agent implementations in the most intuitive and cost-effective way.

![spring ai alibaba jmanus](/img/user/ai/overview/1.0.0/jmanus.png)

Currently, JManus has the following core capabilities:

+ **Fully implements the OpenManus multi-agent product**
JManus fully delivers OpenManus product capabilities. Users can use the product features through the UI interface. JManus can help users complete problem-solving through automatic planning mode.
+ **Seamless MCP (Model Context Protocol) tool integration support**
This means the Agent can not only call local or cloud-based large language models, but also deeply interact with various external services, APIs, databases, etc., greatly expanding application scenarios and capability boundaries.
+ **Native PLAN-ACT mode support**
Enables the Agent to have complex reasoning, step-by-step execution, and dynamic adjustment capabilities, suitable for advanced AI application scenarios such as multi-turn conversations, complex decision-making, and automated processes.
+ **Agent configuration through UI interface**
Developers and operators can flexibly adjust Agent parameters, models, and tools through simple operations on an intuitive web management interface without modifying underlying code. Task planning can also be adjusted, greatly improving usability and operational efficiency.
+ **Automatic generation of SAA-based agent projects**

Users interact with JManus through natural language, generating plans and solidifying them into fixed solutions for specific vertical domains. If you don't want to limit the runtime to the platform, we are exploring deep integration with low-code platforms and framework scaffolding, supporting the conversion of plans into Spring AI Alibaba projects with equivalent capabilities.



The JManus agent platform is still under continuous development. Please follow the [official repository source code](https://github.com/alibaba/spring-ai-alibaba/tree/main/spring-ai-alibaba-jmanus/) and future release updates.

### DeepResearch Agent
DeepResearch is a Deep Research agent developed based on Spring AI Alibaba Graph, including a complete frontend Web UI (under development) and backend implementation. DeepResearch supports a series of carefully designed tools such as Web Search, Crawling, Python script engine, etc., and can leverage LLM and tool capabilities to help users complete various deep research reports.



The following is the DeepResearch multi-agent application architecture:


<img src="/img/user/ai/overview/1.0.0/deepresearch.png" alt="architecture" style="max-width: 740px; height: 508px" />
