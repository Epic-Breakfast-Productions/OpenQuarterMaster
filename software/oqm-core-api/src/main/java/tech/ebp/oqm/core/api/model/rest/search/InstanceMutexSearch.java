package tech.ebp.oqm.core.api.model.rest.search;

import com.mongodb.client.model.Filters;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.ToString;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.InstanceMutex;
import tech.ebp.oqm.core.api.model.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.core.api.service.mongo.search.SearchUtils;

import javax.measure.Quantity;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.*;

@ToString(callSuper = true)
@Getter
public class InstanceMutexSearch extends SearchObject<InstanceMutex> {
	public static InstanceMutexSearch newInstance(){
		return new InstanceMutexSearch();
	}

}
