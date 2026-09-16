package tech.ebp.oqm.lib.moduleDriver.interaction.command.commands.complex;

import tech.ebp.oqm.lib.moduleDriver.interaction.command.Commands;
import tech.ebp.oqm.lib.moduleDriver.interaction.command.commands.Command;
import tech.ebp.oqm.lib.moduleDriver.interaction.command.commands.CommandParsingUtils;
import tech.ebp.oqm.lib.moduleDriver.interaction.command.commands.CommandType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ErrorCommand extends Command {
	
	private static final CommandType TYPE = CommandType.ERROR;
	
	public static ErrorCommand fromSerialLine(String line) {
		String[] returnedParts = CommandParsingUtils.getPartsAndAssertCommand(TYPE, line);
		
		if (returnedParts.length > 1) {
			throw new IllegalArgumentException("Wrong number of parts given in command.");//TODO: proper exception
		}
		if (returnedParts.length == 1) {
			return new ErrorCommand(returnedParts[0]);
		}
		return new ErrorCommand();
	}
	
	@Getter
	private String errInfo = null;
	
	public ErrorCommand() {
		super(TYPE);
	}
	
	public ErrorCommand(String message) {
		this();
		this.setErrInfo(message);
	}
	
	
	@Override
	public String serialLine() {
		if (this.errInfo == null) {
			return Commands.getSimpleCommandString(this.getType());
		} else {
			return Commands.getComplexCommandString(this.getType(), this.getErrInfo());
		}
	}
	
	public ErrorCommand setErrInfo(String errInfo) {
		this.errInfo = errInfo.strip();
		return this;
	}
}
