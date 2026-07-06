package tech.ebp.oqm.plugin.mssController.testResources.modules;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.Command;
import tech.ebp.oqm.plugin.mssController.testResources.modules.engine.TestModuleEngine;

import java.util.Optional;

@SuperBuilder
public abstract class TestModuleInterface implements AutoCloseable {

	@Getter(AccessLevel.PROTECTED)
	private final TestModuleEngine engine;

	@Getter(AccessLevel.PROTECTED)
	private final ObjectMapper objectMapper;


	public TestModuleInterface(ObjectMapper objectMapper, TestModuleEngine engine) {
		this.objectMapper = objectMapper;
		this.engine = engine;
	}

	/**
	 * Initializes and sets up the module interface.
	 */
	public abstract void init() throws Exception;

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
