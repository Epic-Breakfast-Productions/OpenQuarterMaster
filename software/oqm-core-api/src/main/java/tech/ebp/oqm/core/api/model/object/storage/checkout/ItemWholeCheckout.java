package tech.ebp.oqm.core.api.model.object.storage.checkout;

import lombok.*;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.StoredType;

import javax.measure.Quantity;

/**
 * The details used to describe a checked out item
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
//@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class ItemWholeCheckout extends ItemCheckout<Stored> {
	public static final int CUR_SCHEMA_VERSION = 2;

	@Override
	public CheckoutType getCheckoutType(){
		return CheckoutType.WHOLE;
	}

	@Override
	public int getSchemaVersion() {
		return CUR_SCHEMA_VERSION;
	}
}
