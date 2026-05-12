package tech.ebp.oqm.core.api.service.mongo.search;

import lombok.*;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemAwareSearchResult<T extends MainObject> extends SearchResult<T> {

	private InventoryItem inventoryItem;

	public ItemAwareSearchResult(InventoryItem inventoryItem, SearchResult<T> searchResult) {
		super(
			searchResult.getResults(),
			searchResult.getNumResults(),
			searchResult.getNumResultsForEntireQuery(),
			searchResult.isHadSearchQuery(),
			searchResult.getPagingOptions(),
			searchResult.getPagingCalculations(),
			searchResult.getSearchObject()
		);
		this.inventoryItem = inventoryItem;
	}
}
