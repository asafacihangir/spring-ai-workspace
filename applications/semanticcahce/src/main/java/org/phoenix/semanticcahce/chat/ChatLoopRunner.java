package org.phoenix.semanticcahce.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "chat.loop.enabled", havingValue = "true", matchIfMissing = true)
public class ChatLoopRunner implements CommandLineRunner {

	private static final Set<String> EXIT_COMMANDS = Set.of("exit", "quit", "çık");

	private final ChatService chatService;

	public ChatLoopRunner(ChatService chatService) {
		this.chatService = chatService;
	}

	@Override
	public void run(String... args) throws IOException {
		runLoop(new BufferedReader(new InputStreamReader(System.in)), System.out);
	}

	void runLoop(BufferedReader in, PrintStream out) throws IOException {
		out.println("Sohbet başladı. Çıkmak için 'exit' yazın.");
		while (true) {
			out.print("> ");
			String line = in.readLine();
			if (line == null || EXIT_COMMANDS.contains(line.trim().toLowerCase())) {
				out.println("Görüşmek üzere.");
				return;
			}
			String question = line.trim();
			if (question.isEmpty()) {
				continue;
			}
			try {
				long start = System.nanoTime();
				String answer = chatService.ask(question);
				long elapsedMs = (System.nanoTime() - start) / 1_000_000;
				out.println(answer);
				out.println("(" + elapsedMs + " ms)");
			}
			catch (Exception e) {
				out.println("Model çağrısı başarısız: " + e.getMessage());
			}
		}
	}

}
