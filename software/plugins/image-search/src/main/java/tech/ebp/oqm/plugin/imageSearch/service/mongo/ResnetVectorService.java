package tech.ebp.oqm.plugin.imageSearch.service.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import tech.ebp.oqm.plugin.imageSearch.model.resnet.ImageVector;

@ApplicationScoped
public class ResnetVectorService {
	
	@Inject
	@Getter(AccessLevel.PROTECTED)
	MongoClient mongoClient;
	
	protected MongoDatabase getMongoDatabase() {
		return this.getMongoClient().getDatabase("oqm-image-search");
	}
	
	public MongoCollection<ImageVector> getTypedCollection() {
		return this.getMongoDatabase().getCollection("resnet-image-vectors", ImageVector.class);
	}
	
	
	
}
