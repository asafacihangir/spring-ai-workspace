# spring-ai-workspace

Spring AI ile geliştirilen örnek uygulamaların bir araya getirildiği çalışma alanı.

## Uygulamalar

| Uygulama | Açıklama |
|----------|----------|
| [semanticcahce](applications/semanticcahce/README.md) | Spring AI ile semantic caching: anlamca benzer sorularda LLM'i yeniden çağırmadan cache'ten yanıt döner. |
| [agentbehaviorwithskills](applications/agentbehaviorwithskills/README.md) | Spring AI ile skill tabanlı agent davranışı örneği. |
| [skillsjar](applications/skillsjar/README.md) | AI agent skill'lerinin Java `.jar` içinde paketlenmesi; Spring AI ile PDF skill agent örneği. |
| [todowritetool](applications/todowritetool/README.md) | Spring AI ile planlayan agent: görevi `TodoWrite` ile adımlara böler, sırayla tamamlar ve %100'de yanıtı döner. |
| [springai-docling-sample](applications/springai-docling-sample/README.md) | Spring AI + Docling ile kurumsal doküman işleme ve RAG: parsing on-premises (Docling), soru-cevap için OpenAI; hallucination riskini RAG ile azaltır. |
| [multi-agent-rag-spring](applications/multi-agent-rag-spring/README.md) | Adaptive RAG: soruyu yönlendiren, vector store veya web (Tavily) arasından kaynak seçen, yanıt kalitesini denetleyip yetersizse soruyu yeniden yazıp tekrar deneyen çok-ajanlı sistem. |
| [embabel/blog-agent](applications/embabel/blog-agent/README.md) | Embabel Agent Framework ile GOAP tabanlı blog yazma agent'ı: outline → draft → review aşamalarını input/output tiplerinden otomatik plan çıkararak yürütür. |
| [self-correcting-structured-output](applications/self-correcting-structured-output/README.md) | Spring AI 2.0 ile structured output: LLM yanıtını tipli Java nesnesine dönüştürür ve parse hatalarını kendi kendine düzelten mekanizmalarla güvenilirliği artırır. |


## Kaynaklar

- [Craig Walls — Medium](https://thetalkingapp.medium.com/) — Spring AI üzerine yazılar.
- [Thomas Vitale — Docling ile doküman işleme (Java + Arconia + Spring Boot)](https://www.thomasvitale.com/ai-document-processing-docling-java-arconia-spring-boot/)
- [Thomas Vitale — Docling ile RAG (Java + Spring AI)](https://www.thomasvitale.com/rag-docling-java-spring-ai/)
- [Spring Blog — Self-Correcting Structured Output](https://spring.io/blog/2026/06/23/spring-ai-self-correcting-structured-output)