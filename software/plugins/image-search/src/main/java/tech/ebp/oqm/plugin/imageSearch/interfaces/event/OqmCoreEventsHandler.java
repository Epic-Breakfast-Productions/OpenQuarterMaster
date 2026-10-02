package tech.ebp.oqm.plugin.imageSearch.interfaces.event;

import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.*;

import jakarta.enterprise.context.ApplicationScoped;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.messaging.EventNotificationWrapper;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.messaging.EventType;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.messaging.HistoryEventFilter;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.messaging.ObjectType;
import tech.ebp.oqm.plugin.imageSearch.service.mongo.ResnetVectorService;

import java.util.List;

@Slf4j
@ApplicationScoped
public class OqmCoreEventsHandler {

	private static final HistoryEventFilter.FilterOptions IMAGE_FILTER = HistoryEventFilter.FilterOptions.builder().objectType(List.of(ObjectType.Image)).build();
	private static final HistoryEventFilter.FilterOptions NEW_IMAGE_FILTER = HistoryEventFilter.FilterOptions.builder().eventType(List.of(EventType.CREATE, EventType.FILE_NEW_VERSION)).build();
	private static final HistoryEventFilter.FilterOptions DELETED_IMAGE_FILTER = HistoryEventFilter.FilterOptions.builder().eventType(List.of(EventType.DELETE)).build();

	@Inject
	ResnetVectorService resnetVectorService;

	/**
	 * Consume the message from the OQM Core api Events Channel.
	 **/
	@Incoming("oqm-events")
	public void receive(EventNotificationWrapper message) {
		log.debug("Received message: {}", message);
		if(
			HistoryEventFilter.filter(message, IMAGE_FILTER)
		){
			if(HistoryEventFilter.filter(message, NEW_IMAGE_FILTER)){
				log.info("Received {} event for image: {} / {}", message.getEvent().get("type").asText(), message.getEvent().get("objectId").asText(), message);
				this.resnetVectorService.processImage(message.getDatabase(), message.getEvent().get("objectId").asText());
			} else if(HistoryEventFilter.filter(message, DELETED_IMAGE_FILTER)) {
				log.info("Received DELETE event for image: {} / {}", message.getEvent().get("objectId").asText(), message);
				this.resnetVectorService.deleteImage(message.getDatabase(), message.getEvent().get("objectId").asText());
			}
		}

	}
}
