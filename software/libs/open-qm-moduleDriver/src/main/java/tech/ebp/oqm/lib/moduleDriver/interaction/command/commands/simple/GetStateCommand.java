package tech.ebp.oqm.lib.moduleDriver.interaction.command.commands.simple;

import tech.ebp.oqm.lib.moduleDriver.interaction.command.commands.CommandType;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class GetStateCommand extends SimpleCommand {
	
	private static final GetStateCommand INSTANCE = new GetStateCommand();
	
	public static GetStateCommand getInstance() {
		return INSTANCE;
	}
	
	protected GetStateCommand() {
		super(CommandType.GET_STATE);
	}
}
