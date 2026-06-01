package tech.ebp.oqm.plugin.imageSearch.interfaces.event;

import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.*;

import jakarta.enterprise.context.ApplicationScoped;
import tech.ebp.oqm.plugin.imageSearch.service.mongo.ResnetVectorService;

@Slf4j
@ApplicationScoped
public class OqmCoreEventsHandler {

	@Inject
	ResnetVectorService resnetVectorService;

	/**
	 * Consume the message from the OQM Core api Events Channel.
	 * TODO:: tie in with various downstream services
	 **/
	@Incoming("oqm-events")
	public void receive(String message) {
		log.info("Received message: {}", message);
	}
}
