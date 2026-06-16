package tech.ebp.oqm.plugin.mssController.model.moduleComm.command.commands;

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
public class GetModuleInfoCommand extends Command {

	@Override
	public CommandType getCommand() {
		return CommandType.GET_MODULE_INFO;
	}

}
