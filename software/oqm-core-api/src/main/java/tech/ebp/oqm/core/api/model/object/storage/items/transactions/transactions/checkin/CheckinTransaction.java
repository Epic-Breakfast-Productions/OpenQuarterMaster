package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkin;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.checkout.checkinDetails.CheckInDetails;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.ItemStoredTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.TransactionType;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public abstract class CheckinTransaction extends ItemStoredTransaction {

	/**
	 * The id of the checkout to checkin
	 */
	private ObjectId checkoutId;

	/**
	 * The details of checking in the transaction
	 */
	@NotNull
	@NonNull
	private CheckInDetails details;

}
