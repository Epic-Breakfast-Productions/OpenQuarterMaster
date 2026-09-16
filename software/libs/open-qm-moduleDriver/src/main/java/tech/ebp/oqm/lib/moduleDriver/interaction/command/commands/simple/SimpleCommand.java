package tech.ebp.oqm.lib.moduleDriver.interaction.command.commands.simple;

import tech.ebp.oqm.lib.moduleDriver.interaction.command.Commands;
import tech.ebp.oqm.lib.moduleDriver.interaction.command.commands.Command;
import tech.ebp.oqm.lib.moduleDriver.interaction.command.commands.CommandType;
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
