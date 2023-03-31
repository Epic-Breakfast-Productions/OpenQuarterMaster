package tech.ebp.oqm.lib.core.object.itemList;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bson.types.ObjectId;
import tech.ebp.oqm.lib.core.object.storage.items.stored.Stored;

import javax.validation.constraints.NotNull;

/**
 * TODO:: validator to ensure from/to storage ids straight
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemListAction {
	
	/**
	 * The action this action will take.
	 */
	@NonNull
	@NotNull
	private ListItemAction action;
	
	/**
	 * The storage block this action goes to.
	 */
	private ObjectId targetStorageTo;
	
	/**
	 * The storage block this action comes from.
	 */
	private ObjectId targetStorageFrom;
	
	/**
	 * What to apply the action with.
	 */
	@NonNull
	@NotNull
	private Stored stored;
}
