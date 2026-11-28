package tech.ebp.oqm.plugin.imageSearch.service;

import io.quarkus.arc.All;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.plugin.imageSearch.model.SystemStatus;
import tech.ebp.oqm.plugin.imageSearch.service.imageSearch.providers.ImageSearchProvider;

import java.util.List;

@Slf4j
@ApplicationScoped
public class SystemStatusService {

	@Inject
	@All
	List<ImageSearchProvider> providers;

	public SystemStatus getStatus() {
		return SystemStatus.builder()
			.processing(
				providers.stream()
					.map(ImageSearchProvider::isProcessing)
					.reduce(false, Boolean::logicalOr)
			)
			.build();
	}
}
