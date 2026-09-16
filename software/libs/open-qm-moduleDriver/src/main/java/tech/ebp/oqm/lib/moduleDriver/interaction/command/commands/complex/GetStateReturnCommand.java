package tech.ebp.oqm.lib.moduleDriver.interaction.command.commands.complex;

import tech.ebp.oqm.lib.moduleDriver.ModuleState;
import tech.ebp.oqm.lib.moduleDriver.interaction.command.Commands;
import tech.ebp.oqm.lib.moduleDriver.interaction.command.commands.Command;
import tech.ebp.oqm.lib.moduleDriver.interaction.command.commands.CommandParsingUtils;
import tech.ebp.oqm.lib.moduleDriver.interaction.command.commands.CommandType;
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
