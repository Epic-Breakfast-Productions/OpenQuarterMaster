package tech.ebp.oqm.core.api.model.rest.storage.itemCheckout;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.checkout.checkoutFor.CheckoutFor;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;

import java.time.ZonedDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemCheckoutRequest {
	
	@NotNull
	@NonNull
	private ObjectId item;
	
	@NonNull
	@NotNull
	private ObjectId checkedOutFrom;

	/**
	 * What to check out from the item
	 */
	@NonNull
	@NotNull
	private Stored toCheckout;
	
	/**
	 * Who this was checked out for. Null for the JWT user.
	 */
	@lombok.Builder.Default
	private CheckoutFor checkedOutFor = null;

	@lombok.Builder.Default
	private ZonedDateTime dueBack = null;
	
	
	@NonNull
	@NotNull
	@lombok.Builder.Default
	private String reason = "";
	
	@NonNull
	@NotNull
	@lombok.Builder.Default
	private String notes = "";
}
