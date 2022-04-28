package com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.complex;

import com.ebp.openQuarterMaster.lib.driver.interaction.command.Commands;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.Command;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.CommandType;
import lombok.ToString;

import java.util.Arrays;


public abstract class ComplexCommand extends Command {
	
	protected ComplexCommand(CommandType type) {
		super(type);
	}
}
