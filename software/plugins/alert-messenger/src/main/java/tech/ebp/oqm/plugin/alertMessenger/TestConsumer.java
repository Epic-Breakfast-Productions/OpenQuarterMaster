package tech.ebp.oqm.plugin.alertMessenger;


import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

@Slf4j
@ApplicationScoped
public class TestConsumer {

	@Incoming("events-incoming")
	public void toUpperCase(Message<String> message) {
		log.info("Received message {}", message);
	}
}
