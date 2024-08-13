package tech.ebp.oqm.plugin.mssController.testModuleServer.interfaces.serial;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.ScheduledExecution;
import io.quarkus.scheduler.Scheduler;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.plugin.mssController.lib.command.MssCommand;
import tech.ebp.oqm.plugin.mssController.lib.command.response.CommandResponse;
import tech.ebp.oqm.plugin.mssController.testModuleServer.config.ModuleConfig;
import tech.ebp.oqm.plugin.mssController.testModuleServer.module.TestModuleImpl;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@ApplicationScoped
public class SerialModuleInterface {

	@Inject
	TestModuleImpl testModuleImpl;
	@Inject
	ObjectMapper objectMapper;
	TestSerialPort testSerialPort;

	@Inject
	Scheduler scheduler;

	@PostConstruct
	public void init(ModuleConfig config) throws IOException {
		if(config.type() == ModuleConfig.TestModuleType.SERIAL){
			return;
		}

		this.testSerialPort = new TestSerialPort(this.objectMapper);

		this.scheduler.newJob("Serial Port Processing")
			.setInterval("0.25s")
			.setTask(this::process)
			.setConcurrentExecution(Scheduled.ConcurrentExecution.SKIP);
	}

	public void process(ScheduledExecution scheduledExecution) {
		log.debug("In process loop.");

		try {
			Optional<MssCommand> command = this.testSerialPort.getNextCommand();
			if(command.isEmpty()){
				log.debug("No command found.");
				return;
			}
			CommandResponse response = this.testModuleImpl.process(command.get());
			this.testSerialPort.sendCommand(response);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		log.debug("Done with process loop.");
	}
}
