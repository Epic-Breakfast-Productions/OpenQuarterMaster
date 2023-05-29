package tech.ebp.oqm.lib.core.object.storage.items.checkout;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.bson.types.ObjectId;
import tech.ebp.oqm.lib.core.object.AttKeywordMainObject;
import tech.ebp.oqm.lib.core.object.storage.items.InventoryItem;
import tech.ebp.oqm.lib.core.object.storage.items.stored.Stored;
import tech.ebp.oqm.lib.core.object.storage.items.storedWrapper.StoredWrapper;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;

/**
 * The details used to describe an item or set of items checked out
 *
 * While extends `MainObject`, is a sub-object of {@link InventoryItem} rather than first class object.
 *
 * TODO:: need to decide what data should be kept here
 *
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutDetail<S extends Stored> extends AttKeywordMainObject {
	
	/**
	 * When these item(s) were checked out
	 */
	private ZonedDateTime checkoutDate = ZonedDateTime.now();
	
	/**
	 * Who checked out the item
	 */
	@NonNull
	@NotNull
	private ObjectId checkedOutBy;
	
	/**
	 * The storage block this item was checked out from
	 */
	@NonNull
	@NotNull
	private ObjectId checkedOutFrom;
	
	/**
	 * When the item is due back by
	 */
	private ZonedDateTime dueBack = null;
	
	@NonNull
	@NotNull
	private String notes = "";
	
	/**
	 * The exact item being checked out
	 */
	@NonNull
	@NotNull
	private S item;
}
