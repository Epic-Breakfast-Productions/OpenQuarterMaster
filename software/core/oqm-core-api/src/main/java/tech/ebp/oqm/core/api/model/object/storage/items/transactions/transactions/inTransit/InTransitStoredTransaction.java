package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;

/**
 * Superclass for in transit transactions which are dealing with already in transit storeds.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public abstract class InTransitStoredTransaction extends InTransitTransaction {

	@NotNull
	@NonNull
	private ObjectId inTransitStored;
}
