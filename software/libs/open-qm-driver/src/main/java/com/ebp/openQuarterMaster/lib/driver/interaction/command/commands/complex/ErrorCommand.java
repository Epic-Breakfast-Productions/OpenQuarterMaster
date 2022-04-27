package com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.complex;

import com.ebp.openQuarterMaster.lib.driver.ModuleInfo;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.Commands;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.Command;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.CommandParsingUtils;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.CommandType;

import java.time.LocalDate;

public class ErrorCommand extends Command {
	
	private ModuleInfo info;
	
	protected ErrorCommand() {
		super(CommandType.ERROR);
	}
	
	public ErrorCommand(String line) {
		this();
		String[] returnedParts = CommandParsingUtils.getAndAssertCommand(this.getType(), line);
		
		if (returnedParts.length != 4) {
			throw new IllegalArgumentException("Wrong number of parts given in command.");//TODO: proper exception
		}
		
		ModuleInfo.Builder builder = ModuleInfo.builder();
		
		builder.serialNo(returnedParts[0]);
		builder.manufactureDate(LocalDate.parse(returnedParts[1]));
		builder.commSpecVersion(returnedParts[2]);
		builder.numBlocks(Integer.parseInt(returnedParts[3]));
		
		this.info = builder.build();
	}
	
	public ErrorCommand(ModuleInfo info) {
		this();
		this.info = info;
	}
	
	public ModuleInfo getModuleInfo() {
		return this.info.toBuilder().build();
	}
	
	@Override
	public String serialLine() {
		return Commands.getComplexCommandString(
			this.getType().commandChar,
			this.info.getSerialNo(),
			this.info.getManufactureDate().toString(),
			this.info.getCommSpecVersion(),
			Integer.toString(this.info.getNumBlocks())
		);
	}
}
