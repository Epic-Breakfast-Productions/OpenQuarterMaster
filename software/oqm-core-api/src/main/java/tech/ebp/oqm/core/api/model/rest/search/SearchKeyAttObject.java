package tech.ebp.oqm.core.api.model.rest.search;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.conversions.Bson;
import tech.ebp.oqm.core.api.model.object.AttKeywordMainObject;
import tech.ebp.oqm.core.api.service.mongo.search.SearchUtils;

import java.util.List;

@ToString(callSuper = true)
@Setter
@Getter
public abstract class SearchKeyAttObject<T extends AttKeywordMainObject> extends SearchObject<T> {
	//attKeywords
	@QueryParam("keyword")
	List<String> keywords;
	@QueryParam("attributeKey") List<String> attributeKeys;
	@QueryParam("attributeValue") List<String> attributeValues;
	
	@Override
	public List<Bson> getSearchFilters() {
		List<Bson> filters = super.getSearchFilters();
		
		SearchUtils.addKeywordSearchFilter(filters, this.getKeywords());
		SearchUtils.addAttributeSearchFilters(
			filters,
			SearchUtils.attListsToMap(this.getAttributeKeys(), this.getAttributeValues())
		);
		
		return filters;
	}
}
