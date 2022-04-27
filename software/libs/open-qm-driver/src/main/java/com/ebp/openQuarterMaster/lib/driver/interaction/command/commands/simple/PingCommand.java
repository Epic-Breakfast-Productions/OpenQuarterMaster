package com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.simple;

import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.CommandType;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class PingCommand extends SimpleCommand {
	
	private static final PingCommand INSTANCE = new PingCommand();
	
	public static PingCommand getInstance() {
		return INSTANCE;
	}
	
	public PingCommand() {
		super(CommandType.PING);
	}
}
