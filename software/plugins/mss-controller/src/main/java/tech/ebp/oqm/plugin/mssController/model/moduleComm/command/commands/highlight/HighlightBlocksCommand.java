package tech.ebp.oqm.plugin.mssController.model.moduleComm.command.commands.highlight;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.Command;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.CommandType;

@EqualsAndHashCode(callSuper = true)
//@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString(callSuper = true)
public class HighlightBlocksCommand extends Command {

	@Override
	public CommandType getCommand() {
		return CommandType.HIGHLIGHT_BLOCKS;
	}

	private int duration;
	private boolean carry;
	private boolean beep;


}
