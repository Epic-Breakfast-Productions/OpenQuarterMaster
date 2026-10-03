package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.storedToInTransit;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.InTransitStoredTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.InTransitTransaction;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
//@AllArgsConstructor
@NoArgsConstructor
public abstract class StoredToInTransitTransaction extends InTransitTransaction {

}
