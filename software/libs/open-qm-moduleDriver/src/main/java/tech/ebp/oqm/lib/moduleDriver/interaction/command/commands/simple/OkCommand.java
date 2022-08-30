package tech.ebp.oqm.lib.moduleDriver.interaction.command.commands.simple;

import tech.ebp.oqm.lib.moduleDriver.interaction.command.commands.CommandType;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class OkCommand extends SimpleCommand {
	
	private static final OkCommand INSTANCE = new OkCommand();
	
	public static OkCommand getInstance() {
		return INSTANCE;
	}
	
	public OkCommand() {
		super(CommandType.OKAY);
	}
}
