package com.ebp.openQuarterMaster.lib.driver.interaction.command;

import com.ebp.openQuarterMaster.lib.driver.ModuleInfo;
import com.ebp.openQuarterMaster.lib.driver.ModuleState;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.Command;

import java.util.Queue;

public interface ModuleCommander {
	
	public Queue<Command> processLines();
	
	public void ping();
	
	public ModuleInfo getInfo();
	
	public ModuleState getState();
	
	
}
