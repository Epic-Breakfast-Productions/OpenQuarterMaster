package tech.ebp.oqm.core.api.model.object.itemList;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;

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
	private ItemListActionMode mode;
	
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
