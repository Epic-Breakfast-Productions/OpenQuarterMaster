package tech.ebp.oqm.lib.moduleDriver.interaction.command.commands;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public abstract class Command {
	
	@Getter
	private final CommandType type;
	
	protected Command(CommandType type) {
		this.type = type;
	}
	
	public abstract String serialLine();
}
