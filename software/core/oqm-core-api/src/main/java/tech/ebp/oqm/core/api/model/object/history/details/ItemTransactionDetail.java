package tech.ebp.oqm.core.api.model.object.history.details;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@BsonDiscriminator
@Schema(title = "ItemTransactionDetail", description = "Details of transaction that occurred on an item.")
public class ItemTransactionDetail extends HistoryDetail {

	@Schema(description = "The transaction that occurred.")
	private ObjectId inventoryItemTransaction;

	@Override
	@Schema(constValue = "ITEM_TRANSACTION", readOnly = true, required = true, examples = "ITEM_TRANSACTION")
	public HistoryDetailType getType() {
		return HistoryDetailType.ITEM_TRANSACTION;
	}
}
