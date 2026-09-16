package tech.ebp.oqm.lib.moduleDriver.interaction.exceptions;

import tech.ebp.oqm.lib.moduleDriver.interaction.command.commands.Command;
import lombok.Getter;

public class CommandAssertionError extends IllegalStateException {
	
	@Getter
	private final Command command;
	
	public CommandAssertionError(Command command) {
		this.command = command;
	}
	
	public CommandAssertionError(Command command, String s) {
		super(s);
		this.command = command;
	}
	
	public CommandAssertionError(Command command, String message, Throwable cause) {
		super(message, cause);
		this.command = command;
	}
	
	public CommandAssertionError(Command command, Throwable cause) {
		super(cause);
		this.command = command;
	}
}
