package tech.ebp.oqm.plugin.mssController.module.command;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IdentifyModCommand extends MssCommand{
	@Getter
	private static final IdentifyModCommand instance = new IdentifyModCommand();
	
	private int duration = 10;
	
	@Override
	public CommandType getCommand() {
		return CommandType.IDENTIFY_MODULE;
	}
}
