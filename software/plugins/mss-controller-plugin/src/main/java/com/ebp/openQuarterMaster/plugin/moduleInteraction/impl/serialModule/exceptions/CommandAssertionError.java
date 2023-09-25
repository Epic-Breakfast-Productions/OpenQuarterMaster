package com.ebp.openQuarterMaster.plugin.moduleInteraction.impl.serialModule.exceptions;

import com.ebp.openQuarterMaster.plugin.moduleInteraction.command.MssCommand;
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
