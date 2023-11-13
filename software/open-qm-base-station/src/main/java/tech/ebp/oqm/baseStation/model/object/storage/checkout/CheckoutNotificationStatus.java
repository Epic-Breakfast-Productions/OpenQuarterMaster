package tech.ebp.oqm.baseStation.model.object.storage.checkout;

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
	@Builder.Default
	private boolean dueSoon = false;
}
