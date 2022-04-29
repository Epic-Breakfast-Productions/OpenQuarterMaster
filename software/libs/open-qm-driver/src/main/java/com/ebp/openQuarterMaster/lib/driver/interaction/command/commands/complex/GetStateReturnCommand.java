package com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.complex;

import com.ebp.openQuarterMaster.lib.driver.ModuleState;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.Commands;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.Command;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.CommandParsingUtils;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.CommandType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class GetStateReturnCommand extends Command {
	
	private static final CommandType TYPE = CommandType.GET_STATE;
	
	public static GetStateReturnCommand fromSerialLine(String line) {
		String[] returnedParts = CommandParsingUtils.getPartsAndAssertCommand(TYPE, line);
		
		if (returnedParts.length != 4) {
			throw new IllegalArgumentException("Wrong number of parts given in command.");//TODO: proper exception
		}
		
		ModuleState.Builder builder = ModuleState.builder();
		
		builder.online(true);
		
		
		//TODO
		
		return new GetStateReturnCommand(builder.build());
	}
	
	@Getter
	private ModuleState info;
	
	protected GetStateReturnCommand() {
		super(TYPE);
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
