package tech.ebp.oqm.lib.moduleDriver.interaction.serial;

import tech.ebp.oqm.lib.moduleDriver.ModuleInfo;
import tech.ebp.oqm.lib.moduleDriver.ModuleState;
import tech.ebp.oqm.lib.moduleDriver.interaction.command.ModuleCommander;
import tech.ebp.oqm.lib.moduleDriver.interaction.command.commands.Command;
import tech.ebp.oqm.lib.moduleDriver.interaction.command.commands.simple.PingCommand;
import lombok.extern.slf4j.Slf4j;

import java.util.Queue;
import java.util.concurrent.Callable;

@Slf4j
public class SerialPortModuleCommander implements ModuleCommander {
	
	private final SerialPortWrapper wrapper;
	
	public SerialPortModuleCommander(SerialPortWrapper wrapper) {
		this.wrapper = wrapper;
	}
	
	private <T> T lockAndDo(Callable<T> func) {
		try {
			this.wrapper.acquireLock();
		} catch(InterruptedException e) {
			throw new RuntimeException(e);//TODO:: real exception
		}
		try {
			return func.call();
		} catch(Exception e) {
			throw new RuntimeException(e);//TODO:: real exception
		} finally {
			this.wrapper.releaseLock();
		}
	}
	
	@Override
	public Queue<Command> processLines() {
		return this.lockAndDo(this.wrapper::getReceivedCommands);
	}
	
	@Override
	public void ping() {
		this.lockAndDo(()->{
			Command response = wrapper.sendCommand(PingCommand.getInstance());
			
			if (!PingCommand.getInstance().equals(response)) {
				throw new IllegalStateException("Did not get expected response from module.");//TODO:: appropriate exception
			}
			
			return null;
		});
	}
	
	@Override
	public ModuleInfo getInfo() {
		//TODO
		return null;
	}
	
	@Override
	public ModuleState getState() {
		//TODO
		return null;
	}
	
	public void setMessage(String message) {
		//TODO
	}
	
	public void setLightSetting(String message) {
		//TODO
	}
}
