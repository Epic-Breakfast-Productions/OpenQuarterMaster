package tech.ebp.oqm.plugin.mssController.testResources.modules;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.Command;
import tech.ebp.oqm.plugin.mssController.testResources.modules.engine.TestModuleEngine;

import java.util.Optional;

/**
 * Abstract transport layer for {@link TestModule} test harnesses.
 *
 *<p>Subclasses define the actual send/receive channel (serial, network, mock, etc.).
 * This base class wires a Jackson {@link ObjectMapper} so callers can send and receive
 * strongly-typed {@link Command} objects without handling JSON manually.</p>
 */
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

	/** Initialize the transport channel (e.g. open socket, connect serial port). */
	public abstract void init() throws Exception;

	/** Send a raw JSON message over the transport channel. */
	public abstract void send(String message);

	/** Convenience wrapper that serializes {@code command} to JSON before sending. */
	public void send(Command command) throws JsonProcessingException {
		this.send(
			this.getObjectMapper().writeValueAsString(command)
		);
	}

	/** Receive the next raw message, if available. Returns {@link Optional#empty()} on timeout or no data. */
	public abstract Optional<String> receive();

	/** Convenience wrapper that deserializes the received message back to a {@link Command}. */
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
