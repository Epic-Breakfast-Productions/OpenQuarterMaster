package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.general.transfer;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.ItemStoredTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.general.GeneralTransaction;

/**
 * Transaction to handle transferring amounts or stored items around.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public abstract class TransferTransaction extends GeneralTransaction {

}
