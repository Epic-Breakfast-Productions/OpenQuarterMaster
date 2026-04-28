package tech.ebp.oqm.plugin.imageSearch.interfaces.event;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.*;

import jakarta.enterprise.context.ApplicationScoped;

@Slf4j
@ApplicationScoped
public class OqmCoreEventsHandler {
	
	/**
	 * Consume the message from the OQM Core api Events Channel.
	 * TODO:: tie in with various downstream services
	 **/
	@Incoming("oqm-events")
	public void toUpperCase(String message) {
		log.info("Received message: {}", message);
	}
}
