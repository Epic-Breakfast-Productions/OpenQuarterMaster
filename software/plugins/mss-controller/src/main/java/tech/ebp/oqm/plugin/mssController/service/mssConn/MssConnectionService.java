package tech.ebp.oqm.plugin.mssController.service.mssConn;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.vertx.ConsumeEvent;
import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Validator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.plugin.mssController.config.ModuleConfig;
import tech.ebp.oqm.plugin.mssController.model.exception.ModuleSetupFailedException;
import tech.ebp.oqm.plugin.mssController.service.db.ModuleRecordRepository;
import tech.ebp.oqm.plugin.mssController.service.mssConn.connectors.ConnState;
import tech.ebp.oqm.plugin.mssController.service.mssConn.connectors.MssConnector;
import tech.ebp.oqm.plugin.mssController.service.mssConn.connectors.serial.SerialMssConnector;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

@Slf4j
@Getter(AccessLevel.PRIVATE)
@ApplicationScoped
public class MssConnectionService {

	@Getter
	private boolean setUp = false;

	@Inject
	ObjectMapper objectMapper;

	@Inject
	ModuleConfig moduleConfig;

	@Inject
	Validator validator;

	@Inject
	EventBus eventBus;

	@Inject
	ModuleRecordRepository mrr;

	@Getter
	private TreeMap<String, MssConnector> activeConnections = new TreeMap<>();

	@Getter
	private List<ModuleSetupFailedException> moduleSetupFailedExceptions = new ArrayList<>();


	public void cullDisconnectedModules() {
		for(MssConnector curConn : this.getActiveConnections().values()){
			switch (curConn.getConnState()){
				case DEGRADED -> {
					log.warn("Connector for MSS module {} marked as {}", curConn.getModuleInfo().getSerialId(), curConn.getConnState());

					//TODO:: do what?
				}
				case FAIL -> {
					//TODO:: do anything?
				}
			}
		}
	}

	public void scanModules(boolean rescan) {
		for (ModuleConfig.SerialConfig.SerialModuleConfig module : this.moduleConfig.serial().modules()) {

			if(
				rescan &&
				this.getActiveConnections().values().stream()
					.filter((m)->m instanceof SerialMssConnector)
					.anyMatch((m)->((SerialMssConnector) m).getPortPath().equals(module.portPath()))
			){
				continue;
			}

			try {
				SerialMssConnector connector = new SerialMssConnector(
					validator,
					this.objectMapper,
					this.eventBus,
					module,
					moduleConfig.serial().timings()
				);
				this.activeConnections.put(connector.getModuleInfo().getSerialId(), connector);
			} catch(ModuleSetupFailedException e) {
				log.error("Failed to setup serial module: {}", module, e);
				this.moduleSetupFailedExceptions.add(e);
			}
		}

		//TODO:: serial scanning

		//TODO:: net modules
		//TODO:: net scanning

		for(MssConnector curConn : this.getActiveConnections().values()){
			this.getMrr().ensurePresent(curConn);
		}
	}


	public void initializeMssConnections() {
		log.info("Setting up MSS connection service.");

		log.info("Serial modules from config: {}", this.moduleConfig.serial().modules());

		this.scanModules(false);

		this.setUp = true;
	}

	@Scheduled(every = "${moduleConfig.management.scheduledEvery}", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
	public void manageModules() {
		log.debug("Managing Modules.");

		this.cullDisconnectedModules();
		this.scanModules(true);

		log.debug("Done managing modules.");
	}


	@ConsumeEvent("module-event-msg-received")
	void handleMsgReceivedEvent(String moduleSerialId){
		log.info("Received an event that a module has a new message: {}", moduleSerialId);

		//TODO:: act on it
	}


}
