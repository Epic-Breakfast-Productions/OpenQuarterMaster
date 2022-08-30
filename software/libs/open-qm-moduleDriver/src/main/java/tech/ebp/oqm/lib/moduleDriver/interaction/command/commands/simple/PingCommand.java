package tech.ebp.oqm.lib.moduleDriver.interaction.command.commands.simple;

import tech.ebp.oqm.lib.moduleDriver.interaction.command.commands.CommandType;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PingCommand extends SimpleCommand {
	
	private static final PingCommand INSTANCE = new PingCommand();
	
	public static PingCommand getInstance() {
		return INSTANCE;
	}
	
	public PingCommand() {
		super(CommandType.PING);
	}
}
