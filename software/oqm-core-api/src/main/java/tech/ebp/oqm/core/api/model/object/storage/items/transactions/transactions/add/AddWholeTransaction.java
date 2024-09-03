package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.add;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
public class AddWholeTransaction extends AddTransaction {

	@NonNull
	@NotNull
	private Stored toAdd;

	@Override
	public TransactionType getTransactionType() {
		return TransactionType.ADD_WHOLE;
	}

	@Override
	public int getSchemaVersion() {
		return 1;
	}
}
