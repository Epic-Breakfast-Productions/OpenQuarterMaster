package com.ebp.openQuarterMaster.plugin.moduleInteraction.command;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GetModInfoCommand extends MssCommand{
	@Getter
	private static final GetModInfoCommand instance = new GetModInfoCommand();
	
	
	@Override
	public CommandType getCommand() {
		return CommandType.GET_MODULE_INFO;
	}
}
