package com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.simple;

import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.CommandType;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class GetInfoCommand extends SimpleCommand {
	
	private static final GetInfoCommand INSTANCE = new GetInfoCommand();
	
	public static GetInfoCommand getInstance() {
		return INSTANCE;
	}
	
	public GetInfoCommand() {
		super(CommandType.GET_INFO);
	}
}
