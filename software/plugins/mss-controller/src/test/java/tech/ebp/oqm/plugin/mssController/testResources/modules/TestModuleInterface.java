package tech.ebp.oqm.plugin.mssController.testResources.modules;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.Command;

import java.io.IOException;
import java.util.Optional;

public abstract class TestModuleInterface implements AutoCloseable {

	@Getter(AccessLevel.PROTECTED)
	@Setter(AccessLevel.PROTECTED)
	private TestModule module;

	@Getter(AccessLevel.PROTECTED)
	private final ObjectMapper objectMapper;

	public TestModuleInterface(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	/**
	 * Initializes and sets up the module interface.
	 * @param module
	 */
	public abstract void init(TestModule module) throws Exception;

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
