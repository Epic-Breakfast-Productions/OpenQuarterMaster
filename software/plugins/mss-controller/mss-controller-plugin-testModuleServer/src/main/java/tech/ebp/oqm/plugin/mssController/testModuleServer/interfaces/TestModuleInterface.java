package tech.ebp.oqm.plugin.mssController.testModuleServer.interfaces;

import com.fasterxml.jackson.core.JsonProcessingException;
import tech.ebp.oqm.plugin.mssController.lib.command.MssCommand;

import java.util.Optional;

/**
 * Might not need this after all
 */
public interface TestModuleInterface {
	Optional<MssCommand> getNextCommand() throws JsonProcessingException;
	void sendCommand(Object object);
}
