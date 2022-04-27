package com.ebp.openQuarterMaster.lib.driver.interaction.command.commands;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
public abstract class Command {
	
	@Getter
	private final CommandType type;
	
	protected Command(CommandType type) {
		this.type = type;
	}
	
	public abstract String serialLine();
}
