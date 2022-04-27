package com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.simple;

import com.ebp.openQuarterMaster.lib.driver.interaction.command.Commands;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.Command;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.CommandType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public abstract class SimpleCommand extends Command {
	
	private final String serialLine;
	
	protected SimpleCommand(CommandType type) {
		super(type);
		serialLine = Commands.getSimpleCommandString(Commands.Parts.COMMAND_START_CHAR);
	}
	
	@Override
	public String serialLine() {
		return serialLine;
	}
}
