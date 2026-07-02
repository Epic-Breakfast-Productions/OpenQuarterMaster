package tech.ebp.oqm.plugin.mssController.testResources.modules;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.Command;

import java.util.Optional;

public abstract class TestModuleInterface implements AutoCloseable {

	public abstract ObjectMapper getObjectMapper();

	public abstract void send(String message);

	public void send(Command command) throws JsonProcessingException {
		this.send(
			this.getObjectMapper().writeValueAsString(command)
		);
	}

	public abstract Optional<String> receive();

	public Optional<Command> receiveCommand() throws JsonProcessingException {
		Optional<String> op = this.receive();

		if (op.isEmpty()) {
			return Optional.empty();
		}

		return Optional.of(
			this.getObjectMapper().readValue(op.get(), Command.class)
		);
	}
}
