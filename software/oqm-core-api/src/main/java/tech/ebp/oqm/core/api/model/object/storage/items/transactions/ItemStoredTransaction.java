package tech.ebp.oqm.core.api.model.object.storage.items.transactions;

import lombok.Data;
import tech.ebp.oqm.core.api.model.object.Versionable;

@Data
public abstract class ItemStoredTransaction implements Versionable {

	abstract TransactionType getTransactionType();

}
