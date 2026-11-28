package tech.ebp.oqm.plugin.imageSearch.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.plugin.imageSearch.model.SystemStatus;
import tech.ebp.oqm.plugin.imageSearch.service.mongo.ResnetVectorService;

@Slf4j
@ApplicationScoped
public class SystemStatusService {

	@Inject
	ResnetVectorService vectorService;

	public SystemStatus getStatus(){
		return SystemStatus.builder()
			.processing(this.vectorService.isProcessing())
			.build();
	}

}
