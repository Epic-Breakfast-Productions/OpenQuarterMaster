package tech.ebp.oqm.plugin.mssController.model.moduleComm.command.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.Command;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.CommandType;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString(callSuper = true)
public class LockBlocksCommand extends Command {

	@Override
	public CommandType getCommand() {
		return CommandType.LOCK_BLOCK;
	}

	private LockAction action = LockAction.LOCK;
	private List<Integer> storageBlocks;

	public enum LockAction {
		LOCK,
		UNLOCK
	}
}
