package com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.complex;

import com.ebp.openQuarterMaster.lib.driver.ModuleInfo;
import com.ebp.openQuarterMaster.lib.driver.ModuleState;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.Commands;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.Command;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.CommandParsingUtils;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.CommandType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class GetStateReturnCommand extends Command {
	
	@Getter
	private ModuleState info;
	
	protected GetStateReturnCommand() {
		super(CommandType.GET_STATE);
	}
	
	public GetStateReturnCommand(String line) {
		this();
		String[] returnedParts = CommandParsingUtils.getAndAssertCommand(this.getType(), line);
		
		if (returnedParts.length != 4) {
			throw new IllegalArgumentException("Wrong number of parts given in command.");//TODO: proper exception
		}
		
		ModuleState.Builder builder = ModuleState.builder();
		//TODO
		
		this.info = builder.build();
	}
	
	public GetStateReturnCommand(ModuleState info) {
		this();
		this.info = info;
	}
	
	@Override
	public String serialLine() {
		return Commands.getComplexCommandString(
			this.getType()
			//TODO:: this
		);
	}
}
