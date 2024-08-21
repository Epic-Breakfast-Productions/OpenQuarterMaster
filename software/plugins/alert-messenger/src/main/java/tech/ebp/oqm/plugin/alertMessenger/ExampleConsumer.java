package tech.ebp.oqm.plugin.alertMessenger;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@Slf4j
@ApplicationScoped
public class ExampleConsumer {

	@Getter
	private boolean received = false;

	@PostConstruct
	public void init() {
		log.info("Starting ExampleConsumer");
	}

	@Incoming("oqm-core-all-events")
	public void receive(ObjectNode message) {
		//TODO:: use a kafka ui to figure out why no worky
		log.info("Received message {}", message);
		received = true;
	}
}
