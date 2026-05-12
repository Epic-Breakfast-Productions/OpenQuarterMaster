package tech.ebp.oqm.core.api.model.object.storage.checkout;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.measure.Quantity;

/**
 * The details used to describe a checked out item
 */
@SuppressWarnings("rawtypes")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Schema(title = "ItemAmountCheckout", description = "A checkout for a specific amount of a stored item.")
public class ItemAmountCheckout extends ItemCheckout<Quantity> {
	public static final int CUR_SCHEMA_VERSION = 2;

	private ObjectId fromStored;
	private ObjectId fromBlock;
//	private boolean wholeCheckout = false;

	@Override
	@Schema(constValue = "AMOUNT", readOnly = true, required = true, examples = "AMOUNT")
	public CheckoutType getType(){
		return CheckoutType.AMOUNT;
	}

	@Override
	public int getSchemaVersion() {
		return CUR_SCHEMA_VERSION;
	}
}
