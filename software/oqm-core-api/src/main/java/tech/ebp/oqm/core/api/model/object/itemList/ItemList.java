package tech.ebp.oqm.core.api.model.object.itemList;

import jakarta.validation.constraints.NotBlank;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Describes a list of actions to take on the storage of an item
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ItemList extends AttKeywordMainObject {
	public static final int CUR_SCHEMA_VERSION = 1;
	
	/**
	 * The name of this list
	 */
	@NonNull
	@NotNull
	@NotBlank
	private String name;
	
	/**
	 * The description for this list
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	private String description = "";
	
	/**
	 * Map of items to their associated action.
	 *
	 * TODO:: validator
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	private Map<ObjectId, List<@NonNull ItemListAction>> itemActions = new HashMap<>();
	
	/**
	 * If this list was applied or not.
	 */
	@lombok.Builder.Default
	private boolean applied = false;
	
	/**
	 * Gets the list of item actions for the item id given. Adds a new list if one was not present.
	 * @param itemId The id of the item to get the action list for
	 * @return action list for the item given
	 */
	public List<ItemListAction> getItemActions(ObjectId itemId){
		if(!this.getItemActions().containsKey(itemId)){
			this.getItemActions().put(itemId, new ArrayList<>());
		}
		return this.getItemActions().get(itemId);
	}

	@Override
	public int getSchemaVersion() {
		return CUR_SCHEMA_VERSION;
	}
}
