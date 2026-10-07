package tech.ebp.oqm.plugin.mssController.model.moduleComm.message.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.message.Message;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.message.MessageType;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class InventoryEventReport extends Message {

	@Override
	public MessageType getMsgType() {
		return MessageType.INV_EVENT_REPORT;
	}

	private List<BlockWeightReport> weightStateChanges;
	private List<UniqueItemReport> uniqueItemChanges;
	private List<AmountItemReport> amountItemChanges;


}
