package tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkout;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.checkout.CheckoutDetails;
import tech.ebp.oqm.core.api.model.object.storage.checkout.checkoutFor.CheckoutFor;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.ItemStoredTransaction;

import java.time.ZonedDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public abstract class CheckoutTransaction extends ItemStoredTransaction {

	@NotNull
	@NonNull
	private CheckoutDetails checkoutDetails;
}