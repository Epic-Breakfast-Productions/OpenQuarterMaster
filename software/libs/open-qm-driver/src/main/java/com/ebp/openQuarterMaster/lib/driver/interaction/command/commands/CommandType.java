package com.ebp.openQuarterMaster.lib.driver.interaction.command.commands;

import com.ebp.openQuarterMaster.lib.driver.interaction.command.Commands;

/**
 * Enum of the various command types available.
 */
public enum CommandType {
	OKAY('O'),
	ERROR('E'),
	PING('P'),
	GET_INFO('I'),
	GET_STATE('S'),
	MESSAGE('M');
	
	public final char commandChar;
	
	CommandType(char commandChar) {
		this.commandChar = commandChar;
	}
	
	public boolean isType(String line) {
		return Commands.isCommand(line) && line.charAt(1) == this.commandChar;
	}
	
	public static CommandType from(char commandChar) {
		for (CommandType cur : CommandType.values()) {
			if (cur.commandChar == commandChar) {
				return cur;
			}
		}
		return null;
	}
	
	public static CommandType from(String line) {
		for (CommandType cur : CommandType.values()) {
			if (cur.isType(line)) {
				return cur;
			}
		}
		return null;
	}
}
