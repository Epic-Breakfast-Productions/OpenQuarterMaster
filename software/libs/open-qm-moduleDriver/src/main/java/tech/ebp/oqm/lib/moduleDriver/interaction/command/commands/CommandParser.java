package tech.ebp.oqm.lib.moduleDriver.interaction.command.commands;

import tech.ebp.oqm.lib.moduleDriver.interaction.command.Commands;
import tech.ebp.oqm.lib.moduleDriver.interaction.command.commands.complex.ErrorCommand;
import tech.ebp.oqm.lib.moduleDriver.interaction.command.commands.complex.GetInfoReturnCommand;
import tech.ebp.oqm.lib.moduleDriver.interaction.command.commands.complex.GetStateReturnCommand;
import tech.ebp.oqm.lib.moduleDriver.interaction.command.commands.complex.MessageCommand;
import tech.ebp.oqm.lib.moduleDriver.interaction.command.commands.simple.GetInfoCommand;
import tech.ebp.oqm.lib.moduleDriver.interaction.command.commands.simple.GetStateCommand;
import tech.ebp.oqm.lib.moduleDriver.interaction.command.commands.simple.OkCommand;
import tech.ebp.oqm.lib.moduleDriver.interaction.command.commands.simple.PingCommand;
import tech.ebp.oqm.lib.moduleDriver.interaction.exceptions.CommandParseException;

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
