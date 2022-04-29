package com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.complex;

import com.ebp.openQuarterMaster.lib.driver.ModuleInfo;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.Commands;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.Command;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.CommandParsingUtils;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.CommandType;
import com.ebp.openQuarterMaster.lib.driver.interaction.exceptions.CommandParseException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class GetInfoReturnCommand extends Command {
	
	private static final CommandType TYPE = CommandType.GET_INFO;
	
	public static GetInfoReturnCommand fromSerialLine(String line) {
		String[] returnedParts = CommandParsingUtils.getPartsAndAssertCommand(TYPE, line);
		
		if (returnedParts.length != 4) {
			throw new CommandParseException("Wrong number of parts given in command.");
		}
		
		ModuleInfo.Builder builder = ModuleInfo.builder();
		
		builder.serialNo(returnedParts[0]);
		builder.manufactureDate(LocalDate.parse(returnedParts[1]));
		builder.commSpecVersion(returnedParts[2]);
		builder.numBlocks(Integer.parseInt(returnedParts[3]));
		
		return new GetInfoReturnCommand(builder.build());
	}
	
	@Getter
	private ModuleInfo info;
	
	protected GetInfoReturnCommand() {
		super(CommandType.GET_INFO);
	}
	
	public GetInfoReturnCommand(ModuleInfo info) {
		this();
		this.info = info;
	}
	
	@Override
	public String serialLine() {
		return Commands.getComplexCommandString(
			this.getType(),
			this.info.getSerialNo(),
			this.info.getManufactureDate().toString(),
			this.info.getCommSpecVersion(),
			Integer.toString(this.info.getNumBlocks())
		);
	}
}
