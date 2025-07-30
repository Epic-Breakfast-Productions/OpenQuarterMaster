package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.ItemStoredTransaction;

/**
 * Transaction to set specific amounts to.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public abstract class SetTransaction extends ItemStoredTransaction {
}
