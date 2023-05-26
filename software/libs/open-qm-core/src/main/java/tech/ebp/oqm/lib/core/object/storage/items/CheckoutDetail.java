package tech.ebp.oqm.lib.core.object.storage.items;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bson.types.ObjectId;

import java.time.ZonedDateTime;

/**
 * The details used to describe an item or set of items checked out
 *
 * TODO:: need to decide what data should be kept here
 *
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CheckoutDetail {
	
	/**
	 * When these item(s) were checked out
	 */
	private ZonedDateTime checkoutDate = ZonedDateTime.now();
	
	/**
	 * Who checked out the item
	 */
	@NonNull
	private ObjectId checkedOutBy;
	
	private ZonedDateTime dueBack = null;
}
