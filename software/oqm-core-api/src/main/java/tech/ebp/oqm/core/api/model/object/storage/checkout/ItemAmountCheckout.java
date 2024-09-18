package tech.ebp.oqm.core.api.model.object.storage.checkout;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.AttKeywordMainObject;
import tech.ebp.oqm.core.api.model.object.storage.checkout.checkinDetails.CheckInDetails;
import tech.ebp.oqm.core.api.model.object.storage.checkout.checkoutFor.CheckoutFor;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.StoredType;

import javax.measure.Quantity;
import java.time.ZonedDateTime;

/**
 * The details used to describe a checked out item
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class ItemAmountCheckout extends ItemCheckout<Quantity<?>> {
	public static final int CUR_SCHEMA_VERSION = 2;

	private ObjectId fromStoredId;
//	private boolean wholeCheckout = false;

	@Override
	public CheckoutType getCheckoutType(){
		return CheckoutType.AMOUNT;
	}

	@Override
	public int getSchemaVersion() {
		return CUR_SCHEMA_VERSION;
	}
}
