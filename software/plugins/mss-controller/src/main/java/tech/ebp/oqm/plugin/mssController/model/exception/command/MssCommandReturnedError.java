package tech.ebp.oqm.plugin.mssController.model.exception.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.Command;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.response.CommandResponse;


public class MssCommandReturnedError extends MssCommandError {

	@Getter
	private final String serialId;
	@Getter
	private final Command command;
	@Getter
	private final CommandResponse response;

	public MssCommandReturnedError(String serialId, Command command, CommandResponse response){
		this.serialId = serialId;
		this.command = command;
		this.response = response;
		super("MSS command to module with serial id \""+serialId+"\" returned error: " + response.getStatus());
	}
}
