package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.receive;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.InTransit;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.InTransitStoredTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.InTransitTransaction;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public abstract class ReceiveInTransitTransaction extends InTransitStoredTransaction {

	@Nullable
	private ObjectId toBlock;
}
