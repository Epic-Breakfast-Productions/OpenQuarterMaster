package tech.ebp.oqm.core.api.model.rest.search;

import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.ToString;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.AppliedTransaction;
import tech.ebp.oqm.core.api.service.mongo.search.SearchUtils;
import tech.ebp.oqm.core.api.service.mongo.utils.codecs.ZonedDateTimeCodec;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.lte;

@ToString(callSuper = true)
@Getter
public class AppliedTransactionSearch extends SearchKeyAttObject<AppliedTransaction> {
	public static AppliedTransactionSearch newInstance(){
		return new AppliedTransactionSearch();
	}

	@PathParam("itemId")
	ObjectId inventoryItemId;


	@Parameter(description = "The date range to search for.")
	@QueryParam("startDateTime")
	private ZonedDateTime startDateTime;

	@Parameter(description = "The date range to search for.")
	@QueryParam("startDateTime")
	private ZonedDateTime endDateTime;


	//TODO:: More

	@Override
	public List<Bson> getSearchFilters() {
		List<Bson> filters = super.getSearchFilters();

		if (hasValue(this.getInventoryItemId())) {
			filters.add(
				eq("inventoryItem", this.getInventoryItemId())
			);
		}

		if(hasValue(this.getStartDateTime())){
			filters.add(
				gte("timestamp." + ZonedDateTimeCodec.MONGO_INSTANT_FIELD_NAME, this.getStartDateTime().withZoneSameInstant(ZoneOffset.UTC).toInstant())
			);
		}
		if(hasValue(this.getEndDateTime())){
			filters.add(
				lte("timestamp." + ZonedDateTimeCodec.MONGO_INSTANT_FIELD_NAME, this.getEndDateTime().withZoneSameInstant(ZoneOffset.UTC).toInstant())
			);
		}

		return filters;
	}
}
