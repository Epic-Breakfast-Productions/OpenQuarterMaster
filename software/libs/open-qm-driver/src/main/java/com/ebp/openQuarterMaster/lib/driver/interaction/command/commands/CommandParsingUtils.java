package com.ebp.openQuarterMaster.lib.driver.interaction.command.commands;

import com.ebp.openQuarterMaster.lib.driver.interaction.command.Commands;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.complex.ErrorCommand;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.complex.GetInfoReturnCommand;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.complex.GetStateReturnCommand;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.simple.GetInfoCommand;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.simple.GetStateCommand;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.simple.OkCommand;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.simple.PingCommand;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommandParsingUtils {
	
	public static String[] splitLine(String line) {
		return line.strip().split("\\" + Commands.Parts.SEPARATOR_CHAR);
	}
	
	public static String getCommandPart(String[] commandParts) {
		return commandParts[0];
	}
	
	public static char getCommandChar(String[] commandParts) {
		String commandPart = getCommandPart(commandParts);
		
		try {
			return commandPart.charAt(1);
		} catch(IndexOutOfBoundsException e) {
			throw new IllegalArgumentException("Malformed command: " + e);//TODO:: better exception
		}
	}
	
	public static String[] getNonCommandParts(String[] commandParts) {
		if (commandParts.length < 2) {
			return new String[0];
		}
		return Arrays.copyOfRange(commandParts, 1, commandParts.length);
	}
	
	public static String[] getNonCommandParts(String line) {
		return getNonCommandParts(splitLine(line));
	}
	
	public static String[] getAndAssertCommand(CommandType type, String line) {
		String[] commandParts = splitLine(line);
		if (!type.isType(CommandParsingUtils.getCommandPart(commandParts))) {
			throw new IllegalArgumentException("Line given not the correct command type.");//TODO:: better exception
		}
		
		return getNonCommandParts(commandParts);
	}
	
	public static Command parse(String line) {
		if (line == null) {
			throw new IllegalArgumentException("Null line."); //TODO:: better exception
		}
		
		line = line.strip();
		if (line.length() < 2) {
			throw new IllegalArgumentException("Line too short.");
		}
		if (line.charAt(0) != Commands.Parts.COMMAND_START_CHAR) {
			throw new IllegalArgumentException("Line is not a command.");
		}
		CommandType type = CommandType.from(line);
		
		if (type == null) {
			throw new IllegalArgumentException("Could not determine command type.");//TODO:: better exception
		}
		
		if (CommandType.ERROR == type) {
			return new ErrorCommand(line);
		}
		
		if (line.length() == 2) {
			switch (type) {
				case OKAY:
					return OkCommand.getInstance();
				case PING:
					return PingCommand.getInstance();
				case GET_INFO:
					return GetInfoCommand.getInstance();
				case GET_STATE:
					return GetStateCommand.getInstance();
				default:
					throw new IllegalArgumentException("Simple command not recognized.");//TODO:: better exception
			}
		} else {
			switch (type) {
				case GET_INFO:
					return new GetInfoReturnCommand(line);
				case GET_STATE:
					return new GetStateReturnCommand(line);
				default:
					throw new IllegalArgumentException("Complex command not recognized.");
			}
		}
	}
	
}
