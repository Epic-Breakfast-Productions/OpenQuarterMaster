package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkouts.checkout;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.core.api.model.object.storage.checkout.CheckoutDetails;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkouts.CheckoutsTransaction;

/**
 * Transaction to checkout items stored.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public abstract class CheckoutTransaction extends CheckoutsTransaction {

	/**
	 * The details to help inform the checkout procedure.
	 */
	@NotNull
	@NonNull
	private CheckoutDetails checkoutDetails;
}
