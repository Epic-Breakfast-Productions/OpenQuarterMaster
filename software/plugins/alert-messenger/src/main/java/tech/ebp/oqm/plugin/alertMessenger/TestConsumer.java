package tech.ebp.oqm.plugin.alertMessenger;


import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.util.concurrent.CompletionStage;

@Slf4j
@ApplicationScoped
public class TestConsumer {

	@Getter
	private boolean received = false;

	@Incoming("events-incoming")
	public CompletionStage<Void> toUpperCase(Message<String> message) {
		log.info("Received message {}", message);
		received = true;
		return message.ack();
	}
}
