package tech.ebp.oqm.plugin.mssController.testModuleServer;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.plugin.mssController.testModuleServer.config.ModuleConfig;
import tech.ebp.oqm.plugin.mssController.testModuleServer.interfaces.serial.SerialModuleInterface;

@Singleton
@Slf4j
public class ShutdownHandler {
	@Inject
	ModuleConfig moduleConfig;

	@Inject
	SerialModuleInterface serialModuleInterface;

	void onEnd(
		@Observes
		ShutdownEvent ev
	) {
		log.info("CLOSING test module server: \n\t{} \n\t{} \n\t{}",
			this.moduleConfig.type(),
			this.moduleConfig.serialId(),
			this.moduleConfig.numBlocks()
		);

		switch (this.moduleConfig.type()){
			case SERIAL -> {
				this.serialModuleInterface.getTestSerialPort().close();
			}
			case NETWORK -> {
			}
		}

		log.info("MSS Test Module Server shutdown.");
	}
}
