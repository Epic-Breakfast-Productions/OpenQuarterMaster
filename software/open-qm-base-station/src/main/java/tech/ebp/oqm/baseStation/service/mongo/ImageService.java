package tech.ebp.oqm.baseStation.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.baseStation.rest.search.ImageSearch;
import tech.ebp.oqm.baseStation.service.mongo.exception.DbDeleteRelationalException;
import tech.ebp.oqm.lib.core.object.media.Image;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@ApplicationScoped
public class ImageService extends MongoHistoriedObjectService<Image, ImageSearch> {
	
	private StorageBlockService storageBlockService;
	
	ImageService() {//required for DI
		super(null, null, null, null, null, null, false, null);
	}
	
	@Inject
	ImageService(
		//            Validator validator,
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		@ConfigProperty(name = "quarkus.mongodb.database")
			String database,
		StorageBlockService storageBlockService
	) {
		super(
			objectMapper,
			mongoClient,
			database,
			Image.class,
			false
		);
		this.storageBlockService = storageBlockService;
	}
	
	@WithSpan
	@Override
	public void ensureObjectValid(boolean newObject, Image newOrChangedObject, ClientSession clientSession) {
		super.ensureObjectValid(newObject, newOrChangedObject, clientSession);
	}
	
	@WithSpan
	@Override
	protected void assertCanRemove(ClientSession cs, Image objectToRemove) {
		super.assertCanRemove(cs, objectToRemove);
		
		Map<String, Set<ObjectId>> objsWithRefs = new HashMap<>();
		
		Set<ObjectId> storageWithRefs = this.storageBlockService.getObjsReferencing(cs, objectToRemove);
		if(!storageWithRefs.isEmpty()){
			objsWithRefs.put(this.storageBlockService.getClazz().getSimpleName(), storageWithRefs);
		}
		
		if(!objsWithRefs.isEmpty()){
			throw new DbDeleteRelationalException(objectToRemove, objsWithRefs);
		}
	}
}
