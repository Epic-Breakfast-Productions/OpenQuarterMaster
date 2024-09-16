package tech.ebp.oqm.core.api.model.object.storage.checkout;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.AttKeywordMainObject;
import tech.ebp.oqm.core.api.model.object.storage.checkout.checkinDetails.CheckInDetails;
import tech.ebp.oqm.core.api.model.object.storage.checkout.checkinDetails.LossCheckin;
import tech.ebp.oqm.core.api.model.object.storage.checkout.checkinDetails.ReturnCheckin;
import tech.ebp.oqm.core.api.model.object.storage.checkout.checkoutFor.CheckoutFor;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.StoredType;

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
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "checkoutType"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = ItemAmountCheckout.class, name = "AMOUNT"),
	@JsonSubTypes.Type(value = ItemWholeCheckout.class, name = "WHOLE"),
})
@BsonDiscriminator
public abstract class ItemCheckout <T> extends AttKeywordMainObject {
	public static final int CUR_SCHEMA_VERSION = 2;

	public abstract CheckoutType getCheckoutType();
	
	/**
	 * When these item(s) were checked out
	 */
	@lombok.Builder.Default
	private ZonedDateTime checkoutDate = ZonedDateTime.now();
	
	/**
	 * The id of the checked out item
	 */
	@NonNull
	@NotNull
	private ObjectId item;
	
	/**
	 * The storage block this item was checked out from
	 */
	@NonNull
	@NotNull
	private ObjectId checkedOutFrom;

	/**
	 * The transaction that created this checkout.
	 */
	@NonNull
	@NotNull
	private ObjectId transaction;
	
	/**
	 * The exact item being checked out
	 */
	@NonNull
	@NotNull
	private T checkedOut;

	@NotNull
	@NonNull
	private CheckoutDetails checkoutDetails;

	/**
	 * The details of the checkin.
	 */
	@lombok.Builder.Default
	private CheckInDetails checkInDetails = null;
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public boolean isStillCheckedOut(){
		return this.checkInDetails == null;
	};

	@Override
	public int getSchemaVersion() {
		return CUR_SCHEMA_VERSION;
	}
}
