package tech.ebp.oqm.baseStation.service.scheduled;

import io.quarkus.scheduler.Scheduled;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.baseStation.service.mongo.InventoryItemService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Slf4j
@ApplicationScoped
public class ExpiryProcessor {
	
	@Inject
	InventoryItemService inventoryItemService;
	
	@Scheduled(cron = "{service.item.expiryCheck.cron}")
	void searchAndProcessExpiring() {
		log.info("Start processing all held items for newly expired stored.");
		//TODO
	}
}
