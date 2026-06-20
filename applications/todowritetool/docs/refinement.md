Kullanıcıyu bu şekilde kısıtlamak hoşuma gitmedi.
analysis_request: List<String> items, String criteria

Kullanıcı burada olduğu gibi mesajı göndersin ve TodoWrite kullanarak görevlerinizi organize edin.
- Find the top 10 Tom Hanks movies, group them in pairs, and print each title reversed. Use TodoWrite to organize your tasks.

Blog'da advisor kullanılmış,  bizim kullanmamız gerekir mi? Advisor nedir?
`
// Advisors
.defaultAdvisors(
MessageChatMemoryAdvisor.builder(MessageWindowChatMemory.builder().maxMessages(500).build())
.order(Ordered.HIGHEST_PRECEDENCE + 1000).build()`)
				

Logları incelediğim bizim progress 100% 'e ulaşmıyor.
TodoProgressListener, @EventListener'mı kullanmalıydık.