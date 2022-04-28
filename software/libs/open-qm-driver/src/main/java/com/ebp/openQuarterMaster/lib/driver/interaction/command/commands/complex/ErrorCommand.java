package com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.complex;

import com.ebp.openQuarterMaster.lib.driver.ModuleInfo;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.Commands;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.Command;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.CommandParsingUtils;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.CommandType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ErrorCommand extends Command {
	
	@Getter
	@Setter
	private String errInfo = null;
	
	public ErrorCommand() {
		super(CommandType.ERROR);
	}
	
	public ErrorCommand(String line) {
		this();
		String[] returnedParts = CommandParsingUtils.getAndAssertCommand(this.getType(), line);
		
		if (returnedParts.length > 2) {
			throw new IllegalArgumentException("Wrong number of parts given in command.");//TODO: proper exception
		}
		if (returnedParts.length == 2) {
			this.errInfo = returnedParts[1];
		}
	}
	
	
	@Override
	public String serialLine() {
		if (this.errInfo == null) {
			return Commands.getSimpleCommandString(this.getType());
		} else {
			return Commands.getComplexCommandString(this.getType(), this.getErrInfo());
		}
	}
}
