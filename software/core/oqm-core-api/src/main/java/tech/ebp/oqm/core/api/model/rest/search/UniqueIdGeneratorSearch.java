package tech.ebp.oqm.core.api.model.rest.search;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.conversions.Bson;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.unique.UniqueIdentifierGenerator;

import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.or;

@ToString(callSuper = true)
@Getter
@Setter
public class UniqueIdGeneratorSearch extends SearchObject<UniqueIdentifierGenerator> {

	@Override
	public List<Bson> getSearchFilters() {
		List<Bson> filters = super.getSearchFilters();

		return filters;
	}
}
