package tech.ebp.oqm.plugin.mssController.model.moduleComm.command.commands;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.Command;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.CommandType;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString(callSuper = true)
public class PauseReportsCommand extends Command {

	@Override
	public CommandType getCommand() {
		return CommandType.PAUSE_REPORTS;
	}

	@NonNull
	@NotNull
	private PauseAction action;

}
