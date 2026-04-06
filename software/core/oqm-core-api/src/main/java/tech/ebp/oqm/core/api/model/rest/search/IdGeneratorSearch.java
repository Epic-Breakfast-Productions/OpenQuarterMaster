package tech.ebp.oqm.core.api.model.rest.search;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.conversions.Bson;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.generation.GeneratorFor;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.generation.IdentifierGenerator;
import tech.ebp.oqm.core.api.service.mongo.search.SearchUtils;

import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;

@ToString(callSuper = true)
@Getter
@Setter
public class IdGeneratorSearch extends SearchObject<IdentifierGenerator> {
	
	@QueryParam("name")
	String name;
	
	@QueryParam("label")
	String label;
	
	@QueryParam("generatorFor")
	GeneratorFor generatorFor;
	
	@QueryParam("format")
	String format;
	
	@Override
	public List<Bson> getSearchFilters() {
		List<Bson> filters = super.getSearchFilters();
		
		if (this.hasValue(this.name)) {
			filters.add(SearchUtils.getBasicSearchFilter("name", this.name));
		}
		if (this.hasValue(this.label)) {
			filters.add(SearchUtils.getBasicSearchFilter("label", this.label));
		}
		if (this.hasValue(this.format)) {
			filters.add(SearchUtils.getBasicSearchFilter("format", this.format));
		}
		
		if(this.hasValue(this.generatorFor)){
			filters.add(in("forObjectType", this.generatorFor));
		}
		
		return filters;
	}
}
