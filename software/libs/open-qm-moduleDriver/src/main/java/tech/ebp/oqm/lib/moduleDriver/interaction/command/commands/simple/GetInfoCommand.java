package tech.ebp.oqm.lib.moduleDriver.interaction.command.commands.simple;

import tech.ebp.oqm.lib.moduleDriver.interaction.command.commands.CommandType;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class GetInfoCommand extends SimpleCommand {
	
	private static final GetInfoCommand INSTANCE = new GetInfoCommand();
	
	public static GetInfoCommand getInstance() {
		return INSTANCE;
	}
	
	public GetInfoCommand() {
		super(CommandType.GET_INFO);
	}
}
