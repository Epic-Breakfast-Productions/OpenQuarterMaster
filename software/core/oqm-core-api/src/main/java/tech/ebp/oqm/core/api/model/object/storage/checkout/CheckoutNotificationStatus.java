package tech.ebp.oqm.core.api.model.object.storage.checkout;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CheckoutNotificationStatus {
	
	/**
	 * If the checkout obj is due soon or not
	 */
	@lombok.Builder.Default
	private boolean dueSoon = false;
}
