package tech.ebp.oqm.core.api.model.object.history.details;

import lombok.*;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.types.ObjectId;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@BsonDiscriminator
public class ItemTransactionDetail extends HistoryDetail {

	private ObjectId inventoryItemTransaction;

	@Override
	public HistoryDetailType getType() {
		return HistoryDetailType.ITEM_TRANSACTION;
	}
}
