package tech.ebp.oqm.plugin.mssController.model.moduleComm.command;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.commands.CalibrateWeightsCommand;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.commands.ClearHighlightCommand;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.commands.GetModuleInfoCommand;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.commands.GetModuleStateCommand;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.commands.ResetBlockLightsCommand;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.commands.highlight.HighlightBlocksCommand;

@Data
@SuperBuilder
//@AllArgsConstructor
@NoArgsConstructor
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "command"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = CalibrateWeightsCommand.class, name = "CALIBRATE_WEIGHTS"),
	@JsonSubTypes.Type(value = ClearHighlightCommand.class, name = "CLEAR_BLOCK_HIGHLIGHT"),
	@JsonSubTypes.Type(value = GetModuleInfoCommand.class, name = "GET_MODULE_INFO"),
	@JsonSubTypes.Type(value = GetModuleStateCommand.class, name = "GET_MODULE_STATE"),
	@JsonSubTypes.Type(value = HighlightBlocksCommand.class, name = "HIGHLIGHT_BLOCKS"),
	@JsonSubTypes.Type(value = ResetBlockLightsCommand.class, name = "RESET_BLOCK_HIGHLIGHTS"),
})
public abstract class Command {

	public abstract CommandType getCommand();
}
