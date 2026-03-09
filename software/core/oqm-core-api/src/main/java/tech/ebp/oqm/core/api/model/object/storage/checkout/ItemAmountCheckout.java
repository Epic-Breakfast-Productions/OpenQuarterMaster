package tech.ebp.oqm.core.api.model.object.storage.checkout;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;

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
public class ItemAmountCheckout extends ItemCheckout<Quantity> {
	public static final int CUR_SCHEMA_VERSION = 2;

	private ObjectId fromStored;
	private ObjectId fromBlock;
//	private boolean wholeCheckout = false;

	@Override
	public CheckoutType getType(){
		return CheckoutType.AMOUNT;
	}

	@Override
	public int getSchemaVersion() {
		return CUR_SCHEMA_VERSION;
	}
}
