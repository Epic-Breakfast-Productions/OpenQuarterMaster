package com.ebp.openQuarterMaster.lib.driver.interaction.exceptions;

/**
 * Exception denoting there was an issue parsing a command.
 */
public class CommandParseException extends IllegalArgumentException {
	
	public CommandParseException() {
		super();
	}
	
	public CommandParseException(String s) {
		super(s);
	}
	
	public CommandParseException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public CommandParseException(Throwable cause) {
		super(cause);
	}
}
