package tech.ebp.oqm.lib.core.api.quarkus.runtime.scheduled;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.dataHelpers.DataHelperService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.dataHelpers.OqmDatabaseService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.dataHelpers.OqmInfoService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.dataHelpers.OqmUnitService;

/**
 * This service ensures important services are properly initialized.
 *
 * In usage, due to the possibly blocking nature of setup, can cause issues if directly used in a reactive manner. This preemptive init circumvents this.
 */
@Singleton
@Slf4j
public class OqmCoreApiClientInitServices {
	
	@Inject
	OqmDatabaseService oqmDatabaseService;
	@Inject
	OqmInfoService infoService;
	@Inject
	OqmUnitService unitService;
	
	void onStart(
		@Observes
		StartupEvent ev
	) {
		log.info("Pre-initializing caching services.");
		
		if(!DataHelperService.oidcSetup()){
			log.warn("OIDC not setup. Can't init things.");
		} else {
			this.oqmDatabaseService.getDatabases();
			this.infoService.getCurrency();
			this.unitService.getAllUnits();
		}
		
		log.info("Done pre-initializing.");
	}
	
}
