package tech.ebp.oqm.lib.moduleDriver.interaction.command;

import tech.ebp.oqm.lib.moduleDriver.ModuleInfo;
import tech.ebp.oqm.lib.moduleDriver.ModuleState;
import tech.ebp.oqm.lib.moduleDriver.interaction.command.commands.Command;

import java.util.Queue;

public interface ModuleCommander {
	
	public Queue<Command> processLines();
	
	public void ping();
	
	public ModuleInfo getInfo();
	
	public ModuleState getState();
	
	
}
