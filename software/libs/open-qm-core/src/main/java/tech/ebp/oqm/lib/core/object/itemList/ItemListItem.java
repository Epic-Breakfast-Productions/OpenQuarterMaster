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
public class ItemListItem {
	
	@NonNull
	@NotNull
	private ObjectId item;
	
	@NonNull
	@NotNull
	private ListItemAction action;
	
	private ObjectId targetStorageTo;
	private ObjectId targetStorageFrom;
	
	@NonNull
	@NotNull
	private Stored stored;
}
