package tech.ebp.oqm.core.api.model.rest.search;

import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.UniqueIdentifierGenerator;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;

import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.ne;
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
