package tech.ebp.oqm.core.api.model.object.storage.checkout;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;

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
	
	public ObjectId getFromBlock() {
		return this.getCheckedOut().getStorageBlock();
	}

	@Override
	public CheckoutType getType(){
		return CheckoutType.WHOLE;
	}

	@Override
	public int getSchemaVersion() {
		return CUR_SCHEMA_VERSION;
	}
}
