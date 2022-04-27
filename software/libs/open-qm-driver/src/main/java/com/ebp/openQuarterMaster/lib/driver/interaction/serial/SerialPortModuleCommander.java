package com.ebp.openQuarterMaster.lib.driver.interaction.serial;

import com.ebp.openQuarterMaster.lib.driver.ModuleInfo;
import com.ebp.openQuarterMaster.lib.driver.ModuleState;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.Commands;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.ModuleCommander;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.Command;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.CommandParsingUtils;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.simple.PingCommand;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;

@Slf4j
public class SerialPortModuleCommander implements ModuleCommander {
	
	private final SerialPortWrapper wrapper;
	private final Queue<Command> receivedCommands = new LinkedList<>();
	
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
		this.lockAndDo(()->{
			String curLine;
			do {
				curLine = wrapper.readLine();
				
				if (Commands.isLog(curLine)) {
					log.info("LOG FROM MODULE: {}", curLine);
				}
				if (Commands.isCommand(curLine)) {
					this.receivedCommands.add(CommandParsingUtils.parse(curLine));
				}
			} while (curLine != null);
			return null;
		});
		return this.receivedCommands;
	}
	
	@Override
	public void ping() {
		this.lockAndDo(()->{
			wrapper.writeLine(PingCommand.getInstance().serialLine());
			String response = wrapper.readLatestResponse();
			
			if (!PingCommand.getInstance().serialLine().equals(response)) {
				throw new IllegalStateException("Did not get expected response from module.");//TODO:: appropriate exception
			}
			
			return null;
		});
	}
	
	@Override
	public ModuleInfo getInfo() {
		return null;
	}
	
	@Override
	public ModuleState getState() {
		return null;
	}
}
