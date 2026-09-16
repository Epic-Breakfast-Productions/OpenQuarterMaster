package tech.ebp.oqm.lib.moduleDriver.interaction.command.commands.complex;

import tech.ebp.oqm.lib.moduleDriver.interaction.command.commands.Command;
import tech.ebp.oqm.lib.moduleDriver.interaction.command.commands.CommandType;


public abstract class ComplexCommand extends Command {
	
	protected ComplexCommand(CommandType type) {
		super(type);
	}
}
