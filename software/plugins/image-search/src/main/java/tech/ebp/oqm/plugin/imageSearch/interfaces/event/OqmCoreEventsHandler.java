package tech.ebp.oqm.plugin.imageSearch.interfaces.event;

import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.*;

import jakarta.enterprise.context.ApplicationScoped;
import tech.ebp.oqm.plugin.imageSearch.service.mongo.ResnetVectorService;

import java.util.Arrays;
import java.util.Optional;

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
	public void receive(EventNotificationWrapper message) {
		log.info("Received message: {}", message);
		if(//TODO:: update to better mechanisms when they exist
			message.getObjectName().equals("Image")
		){
			if(
				message.getEvent().get("type").asText().equals("CREATE") ||
				message.getEvent().get("type").asText().equals("FILE_NEW_VERSION")
			){
				log.info("Received {} event for image: {} / {}", message.getEvent().get("type").asText(), message.getEvent().get("objectId").asText(), message);
				this.resnetVectorService.processImage(message.getDatabase(), message.getEvent().get("objectId").asText());
			} else if(message.getEvent().get("type").asText().equals("DELETE")) {
				log.info("Received DELETE event for image: {} / {}", message.getEvent().get("objectId").asText(), message);
				this.resnetVectorService.deleteImage(message.getDatabase(), message.getEvent().get("objectId").asText());
			}
		}

	}
}
