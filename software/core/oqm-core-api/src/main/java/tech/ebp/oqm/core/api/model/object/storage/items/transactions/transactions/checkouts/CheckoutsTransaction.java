package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkouts;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.ItemStoredTransaction;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
//@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public abstract class CheckoutsTransaction extends ItemStoredTransaction {

}
