package com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.simple;

import com.ebp.openQuarterMaster.lib.driver.interaction.command.Commands;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.Command;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.CommandType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public abstract class SimpleCommand extends Command {
	
	private final String serialLine;
	
	protected SimpleCommand(CommandType type) {
		super(type);
		this.serialLine = Commands.getSimpleCommandString(this.getType());
	}
	
	@Override
	public String serialLine() {
		return serialLine;
	}
}
