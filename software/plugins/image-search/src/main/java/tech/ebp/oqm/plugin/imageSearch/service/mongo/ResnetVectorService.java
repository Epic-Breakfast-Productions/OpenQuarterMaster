package tech.ebp.oqm.plugin.imageSearch.service.mongo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.ImageSearch;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.sso.KcClientAuthService;
import tech.ebp.oqm.plugin.imageSearch.model.resnet.ImageVector;

import tech.ebp.oqm.plugin.imageSearch.service.ImageSearchService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
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
	
	@RestClient
	OqmCoreApiClientService oqmCoreApiClientService;
	
	@Inject
	KcClientAuthService serviceAccountService;
	
	@Inject
	ImageSearchService imageSearchService;
	
	@Inject
	tech.ebp.oqm.plugin.imageSearch.interfaces.ImageSearch imageSearch;
	
	
	protected MongoDatabase getMongoDatabase() {
		return this.getMongoClient().getDatabase(this.getDatabase());
	}
	
	public MongoCollection<ImageVector> getTypedCollection() {
		return this.getMongoDatabase().getCollection("resnet-image-vectors", ImageVector.class);
	}
	
	public Iterator<ImageVector> getAllVectors(String database) {
		return this.getTypedCollection().find(
			Filters.eq("database", database)
		).iterator();
		
		//example
//		and(
//			Filters.eq("imageId", id),
//			Filters.eq("database", database)
//		)
	}
	
	private void processImage(String database, String imageId, int imageRevision) {
		log.info("Processing image revision: {}, revision: {}", imageId, imageRevision);
		
		//TODO:: check if already processed, skip if exists
		
		try (
			InputStream is = this.oqmCoreApiClientService.imageGetRevisionData(
				this.serviceAccountService.getAuthString(),
				database,
				imageId,
				imageRevision + ""
			).await().indefinitely()
		) {
			ImageVector.ImageVectorBuilder builder = ImageVector.builder();
			
			builder.imageId(imageId);
			builder.imageRevision(imageRevision);
			builder.vector(ImageSearchService.generateImageFeatureVector(is));
			
			this.getTypedCollection().insertOne(builder.build());
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void processImage(String oqmDatabase, ObjectNode imageMetadata) {
		String imageId = imageMetadata.get("id").asText();
		int numRevisions = imageMetadata.get("numRevisions").asInt();
		
		log.info("Processing image: {}, # revisions: {}", imageId, numRevisions);
		
		for (int i = 1; i <= numRevisions; i++) {
			this.processImage(oqmDatabase, imageId, i);
		}
	}
	
	//Iterates db, operate on each image in turn
	public void initVectors() {
		log.info("Processing images in all databases.");
		ImageSearch imageSearch = new ImageSearch();
		imageSearch.setPageNum(0);
		imageSearch.setPageSize(100);
		
		
		//TODO:: iterate through all databases; this.oqmCoreApiClientService.manageDbList(this.serviceAccountService.getAuthString()).await().indefinitely();
		
		String curOqmDb = "default";
		ObjectNode results;
		
		log.info("Processing images database: {}", curOqmDb);
		
		do {
			results = this.oqmCoreApiClientService.imageSearch(
				this.serviceAccountService.getAuthString(),
				curOqmDb,
				imageSearch
			).await().indefinitely();
			
			for (JsonNode curImageResult : results.get("results")) {
				this.processImage(curOqmDb, (ObjectNode) curImageResult);
			}
			
			imageSearch.setPageNum(imageSearch.getPageNum() + 1);
		} while (!results.get("paginationCalculations").get("onLastPage").asBoolean());
		
		// TODO:: remove vectors not in oqm core db
	}
	
}
