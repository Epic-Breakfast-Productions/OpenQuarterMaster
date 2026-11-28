package tech.ebp.oqm.plugin.imageSearch.service.imageSearch.providers.resnet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.plugin.imageSearch.model.resnet.ImageVector;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;

import static com.mongodb.client.model.Filters.and;

@Slf4j
@ApplicationScoped
public class ResnetVectorService {

	@Inject
	@Getter(AccessLevel.PROTECTED)
	MongoClient mongoClient;

	@Getter
	@ConfigProperty(name = "quarkus.mongodb.database")
	String database;

	protected MongoDatabase getMongoDatabase() {
		return this.getMongoClient().getDatabase(this.getDatabase());
	}

	public MongoCollection<ImageVector> getTypedCollection() {
		return this.getMongoDatabase().getCollection("resnet-image-vectors", ImageVector.class);
	}

	public long getNumVectors(String database) {
		return this.getTypedCollection().countDocuments(Filters.eq("oqmDb", database));
	}

	public Iterator<ImageVector> getAllVectors(String database) {
		return this.getTypedCollection().find(Filters.eq("oqmDb", database)).iterator();
	}

	public Iterator<ImageVector> getAllVectors(){
		return this.getTypedCollection().find().iterator();
	}


	public void deleteImage(String database, String imageId) {
		this.getTypedCollection().deleteMany(Filters.and(Filters.eq("imageId", imageId), Filters.eq("oqmDb", database)));
	}

	public void deleteAll(){
		this.getTypedCollection().deleteMany(Filters.empty());
	}

	public boolean vectorExists(String imageId, int imageRevision){
		return this.getTypedCollection().find(
			Filters.and(
				Filters.eq("imageId", imageId),
				Filters.eq("revision", imageRevision)
			)
		).first() != null;
	}

	public void addVector(ImageVector vector){
		this.getTypedCollection().insertOne(vector);
	}

	public void deleteVector(ImageVector imageVector){
		this.getTypedCollection().deleteOne(Filters.eq("_id", imageVector.getId()));
	}
}
