package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkin;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.checkout.checkinDetails.CheckInDetails;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.ItemStoredTransaction;

/**
 * Transaction to checkin a checked out item stored.
 * @param <T> The type of checkin details to use.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public abstract class CheckinTransaction <T extends CheckInDetails> extends ItemStoredTransaction {

	/**
	 * The id of the checkout to checkin
	 */
	private ObjectId checkoutId;

	/**
	 * The details of checking in the transaction
	 */
	@NotNull
	@NonNull
	private T details;

}
