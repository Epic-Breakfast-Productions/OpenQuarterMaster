package com.ebp.openQuarterMaster.lib.driver.interaction.command.commands;

import com.ebp.openQuarterMaster.lib.driver.interaction.command.Commands;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.complex.ErrorCommand;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.complex.GetInfoReturnCommand;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.complex.GetStateReturnCommand;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.complex.MessageCommand;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.simple.GetInfoCommand;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.simple.GetStateCommand;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.simple.OkCommand;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.simple.PingCommand;
import com.ebp.openQuarterMaster.lib.driver.interaction.exceptions.CommandParseException;

public class CommandParser {
	
	public static Command parse(String line) {
		if (line == null) {
			throw new CommandParseException("Null line.", new NullPointerException());
		}
		if (line.isEmpty() || line.isBlank()) {
			throw new CommandParseException("Line is empty.");
		}
		
		line = line.strip();
		if (line.length() < 2) {
			throw new CommandParseException("Line too short.");
		}
		if (line.charAt(0) != Commands.Parts.COMMAND_START_CHAR) {
			throw new CommandParseException("Line is not a command.");
		}
		CommandType type = CommandType.from(line);
		
		if (type == null) {
			throw new CommandParseException("Could not determine command type.");
		}
		
		if (CommandType.ERROR == type) {
			return ErrorCommand.fromSerialLine(line);
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
					throw new CommandParseException("Simple command not recognized.");
			}
		} else {
			switch (type) {
				case GET_INFO:
					return GetInfoReturnCommand.fromSerialLine(line);
				case GET_STATE:
					return GetStateReturnCommand.fromSerialLine(line);
				case MESSAGE:
					return MessageCommand.fromSerialLine(line);
				default:
					throw new CommandParseException("Complex command not recognized.");
			}
		}
	}
}
