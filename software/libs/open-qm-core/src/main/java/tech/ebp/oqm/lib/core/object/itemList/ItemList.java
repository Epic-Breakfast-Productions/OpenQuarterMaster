package tech.ebp.oqm.lib.core.object.itemList;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.bson.types.ObjectId;
import tech.ebp.oqm.lib.core.object.AttKeywordMainObject;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
public class ItemList extends AttKeywordMainObject {
	
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
	private String description = "";
	
	/**
	 * Map of items to their associated action.
	 *
	 * TODO:: validator
	 */
	@NonNull
	@NotNull
	private Map<ObjectId, List<@NonNull ItemListAction>> items = new HashMap<>();
	
	/**
	 * If this list was applied or not.
	 */
	private boolean applied = false;
	
	/**
	 * Gets the list of item actions for the item id given. Adds a new list if one was not present.
	 * @param itemId The id of the item to get the action list for
	 * @return action list for the item given
	 */
	public List<ItemListAction> getItemActions(ObjectId itemId){
		if(!this.getItems().containsKey(itemId)){
			this.getItems().put(itemId, new ArrayList<>());
		}
		return this.getItems().get(itemId);
	}
}
