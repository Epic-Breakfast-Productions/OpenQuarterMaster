package com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.complex;

import com.ebp.openQuarterMaster.lib.driver.interaction.command.Commands;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.Command;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.CommandParsingUtils;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.CommandType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class MessageCommand extends Command {
	
	private static final CommandType TYPE = CommandType.MESSAGE;
	
	public static MessageCommand fromSerialLine(String line) {
		String[] returnedParts = CommandParsingUtils.getPartsAndAssertCommand(TYPE, line);
		
		if (returnedParts.length != 1) {
			throw new IllegalArgumentException("Wrong number of parts given in command.");//TODO: proper exception
		}
		return new MessageCommand(returnedParts[0]);
	}
	
	@Getter
	private String message = null;
	
	public MessageCommand() {
		super(TYPE);
	}
	
	public MessageCommand(String message) {
		this();
		this.setMessage(message);
	}
	
	@Override
	public String serialLine() {
		if (this.message == null) {
			throw new IllegalArgumentException("No message given to send.");//TODO:: proper exception
		} else {
			return Commands.getComplexCommandString(this.getType(), this.message);
		}
	}
	
	public MessageCommand setMessage(String message) {
		this.message = message.strip();
		return this;
	}
}
