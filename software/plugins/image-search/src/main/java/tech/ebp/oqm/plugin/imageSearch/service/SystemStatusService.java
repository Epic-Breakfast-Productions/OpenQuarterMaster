package tech.ebp.oqm.plugin.imageSearch.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.plugin.imageSearch.model.SystemStatus;
import tech.ebp.oqm.plugin.imageSearch.service.mongo.ResnetVectorService;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@ApplicationScoped
public class SystemStatusService {

	@Inject
	ResnetVectorService vectorService;

	public SystemStatus getStatus(){
		boolean processing = vectorService.getLastProcess() != null &&
			Duration.between(vectorService.getLastProcess(), Instant.now()).getSeconds() < 5;

		log.debug("Last processed vector: {}/{}/{}", vectorService.getLastProcess(), Duration.between(vectorService.getLastProcess(), Instant.now()), processing);


		return SystemStatus.builder()
			.processing(processing)
			.build();
	}

}
