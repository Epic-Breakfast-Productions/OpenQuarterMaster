package tech.ebp.oqm.plugin.mssController.testModuleServer;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.plugin.mssController.testModuleServer.config.ModuleConfig;

@Singleton
@Slf4j
public class StartupHandler {
	@Inject
	ModuleConfig moduleConfig;


	void onStart(
		@Observes
		StartupEvent ev
	) {
		log.info("Starting test module server: \n\t{} \n\t{} \n\t{}",
			this.moduleConfig.type(),
			this.moduleConfig.serialId(),
			this.moduleConfig.numBlocks()
		);

		log.info("MSS Test Module Server started.");
	}
}
