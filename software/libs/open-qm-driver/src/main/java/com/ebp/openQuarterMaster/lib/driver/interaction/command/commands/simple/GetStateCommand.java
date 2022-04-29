package com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.simple;

import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.CommandType;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class GetStateCommand extends SimpleCommand {
	
	private static final GetStateCommand INSTANCE = new GetStateCommand();
	
	public static GetStateCommand getInstance() {
		return INSTANCE;
	}
	
	protected GetStateCommand() {
		super(CommandType.GET_STATE);
	}
}
