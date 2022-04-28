package com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.simple;

import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.CommandType;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class OkCommand extends SimpleCommand {
	
	private static final OkCommand INSTANCE = new OkCommand();
	
	public static OkCommand getInstance() {
		return INSTANCE;
	}
	
	public OkCommand() {
		super(CommandType.OKAY);
	}
}
