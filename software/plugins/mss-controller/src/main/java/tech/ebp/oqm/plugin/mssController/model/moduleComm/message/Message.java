package tech.ebp.oqm.plugin.mssController.model.moduleComm.message;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.CommandType;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.commands.CalibrateWeightsCommand;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.commands.ClearHighlightCommand;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.commands.GetModuleInfoCommand;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.commands.GetModuleStateCommand;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.commands.LockBlocksCommand;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.commands.NotifyUserCommand;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.commands.PauseReportsCommand;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.commands.highlight.HighlightBlocksCommand;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.message.report.InventoryEventReport;

@Data
@SuperBuilder
//@AllArgsConstructor
@NoArgsConstructor
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "msgType"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = InventoryEventReport.class, name = "INV_EVENT_REPORT"),

})
public abstract class Message {

	public abstract MessageType getMsgType();
}
