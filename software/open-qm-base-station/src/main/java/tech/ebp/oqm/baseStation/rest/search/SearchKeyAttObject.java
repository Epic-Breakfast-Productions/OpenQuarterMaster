package tech.ebp.oqm.baseStation.rest.search;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.ToString;
import org.bson.conversions.Bson;
import tech.ebp.oqm.baseStation.model.object.AttKeywordMainObject;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchUtils;

import java.util.List;

@ToString(callSuper = true)
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
	
	protected boolean hasValue(String val){
		return val != null && !val.isBlank();
	}
	
	protected boolean hasValue(Boolean val){
		return val != null;
	}
}
