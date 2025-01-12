package tech.ebp.oqm.core.api.model.object.storage.checkout;


import jakarta.validation.constraints.NotNull;
import lombok.*;
import tech.ebp.oqm.core.api.model.object.storage.checkout.checkoutFor.CheckoutFor;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CheckoutDetails {

	/**
	 * Who checked out the item
	 */
	@NonNull
	@NotNull
	private CheckoutFor checkedOutFor;

	/**
	 * When the item is due back by
	 */
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
