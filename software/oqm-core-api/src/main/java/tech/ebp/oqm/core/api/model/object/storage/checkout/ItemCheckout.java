package tech.ebp.oqm.core.api.model.object.storage.checkout;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.AttKeywordMainObject;
import tech.ebp.oqm.core.api.model.object.storage.checkout.checkinDetails.CheckInDetails;
import tech.ebp.oqm.core.api.model.object.storage.checkout.checkoutFor.CheckoutFor;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;

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
public class ItemCheckout extends AttKeywordMainObject {
	public static final int CUR_SCHEMA_VERSION = 1;
	
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
	 * The exact item being checked out
	 */
	@NonNull
	@NotNull
	private Stored checkedOut;
	
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
	
	/**
	 * The exact item being checked out
	 */
	@lombok.Builder.Default
	private CheckInDetails checkInDetails = null;
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public boolean isStillCheckedOut(){
		return this.checkInDetails == null;
	};

	@Override
	public int getSchemaVersion() {
		return 1;
	}
}
