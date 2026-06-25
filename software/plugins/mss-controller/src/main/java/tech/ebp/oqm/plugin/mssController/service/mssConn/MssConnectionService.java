package tech.ebp.oqm.plugin.mssController.service.mssConn;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.plugin.mssController.config.ModuleConfig;
import tech.ebp.oqm.plugin.mssController.model.exception.ModuleSetupFailedException;
import tech.ebp.oqm.plugin.mssController.service.mssConn.serial.SerialMssConnector;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@ApplicationScoped
public class MssConnectionService {

	@Getter
	private boolean setUp = false;

	@Inject
	ObjectMapper objectMapper;

	@Inject
	ModuleConfig moduleConfig;

	@Getter
	private List<MssConnector> connectors = new ArrayList<>();

	@Getter
	private List<ModuleSetupFailedException> moduleSetupFailedExceptions = new ArrayList<>();


	public void initializeMssConnections(){
		log.info("Setting up MSS connection service.");

		log.info("Serial modules from config: {}", this.moduleConfig.serial().modules());

		for (ModuleConfig.SerialConfig.SerialModuleConfig module : this.moduleConfig.serial().modules()) {

			try {
				SerialMssConnector connector = new SerialMssConnector(
					this.objectMapper,
					module,
					moduleConfig.serial().timings()
				);
				this.connectors.add(connector);
			} catch(ModuleSetupFailedException e) {
				log.error("Failed to setup serial module: {}", module, e);
				this.moduleSetupFailedExceptions.add(e);
			}


		}

		//TODO:: serial scanning

		//TODO:: net modules
		//TODO:: net scanning

		this.setUp = true;
	}



}
