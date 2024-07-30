package tech.ebp.oqm.plugin.mssController.moduleInteraction.module.serialModule.exceptions;

import tech.ebp.oqm.plugin.mssController.lib.command.MssCommand;
import lombok.Getter;

public class CommandAssertionError extends IllegalStateException {
	
	@Getter
	private final MssCommand command;
	
	public CommandAssertionError(MssCommand command) {
		this.command = command;
	}
	
	public CommandAssertionError(MssCommand command, String s) {
		super(s);
		this.command = command;
	}
	
	public CommandAssertionError(MssCommand command, String message, Throwable cause) {
		super(message, cause);
		this.command = command;
	}
	
	public CommandAssertionError(MssCommand command, Throwable cause) {
		super(cause);
		this.command = command;
	}
}
