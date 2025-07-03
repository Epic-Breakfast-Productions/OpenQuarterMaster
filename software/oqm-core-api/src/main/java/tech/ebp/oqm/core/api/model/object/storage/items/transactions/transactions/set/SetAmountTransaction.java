package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.set;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;

import javax.measure.Quantity;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class SetAmountTransaction extends SetTransaction {

	private ObjectId stored;
	private ObjectId block;

	@NonNull
	@NotNull
	private Quantity<?> amount;

	@Override
	public TransactionType getType() {
		return TransactionType.SET_AMOUNT;
	}

	@Override
	public int getSchemaVersion() {
		return 1;
	}
}
