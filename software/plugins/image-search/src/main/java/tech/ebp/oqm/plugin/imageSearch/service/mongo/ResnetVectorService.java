package tech.ebp.oqm.plugin.imageSearch.service.mongo;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.ImageSearch;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.sso.KcClientAuthService;
import tech.ebp.oqm.plugin.imageSearch.model.resnet.ImageVector;

import tech.ebp.oqm.plugin.imageSearch.service.ImageSearchService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

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
	
	private void processImage(String database, ObjectNode imageMetadata) {
		try (
			InputStream is = this.oqmCoreApiClientService.imageGetRevisionData(
				this.serviceAccountService.getAuthString(),
				database,
				imageMetadata.get("id").asText(),
				"latest"
			).await().indefinitely()
		) {
			
			//TODO:: process image data (get vector), add/ update mongodb entry for that vector
			File tmpFile = new File("tmp");
			Files.copy(is, tmpFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			float[] vector = ImageSearchService.extractDeepFeatures(tmpFile);
			//HOW TO GET INT OF imageRevision
			ImageVector curObj = new ImageVector(null, "oqm-image-search",
					imageMetadata.get("id").asText(), "latest", vector);

			this.getTypedCollection().insertOne(curObj);
			
			
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}


	//Iterates db, operate on each image in turn
	public void processImages() {
		
		ImageSearch imageSearch = new ImageSearch();
		imageSearch.setPageNum(0);
		imageSearch.setPageSize(100);
		
		ObjectNode results;
		
		do {
			results = this.oqmCoreApiClientService.imageSearch(
				this.serviceAccountService.getAuthString(),
				"default",
				imageSearch
			).await().indefinitely();
			//TODO process each image in result
			processImage("oqm-image-search", results);
			
			imageSearch.setPageNum(imageSearch.getPageNum() + 1);
		} while (!results.get("paginationCalculations").get("onLastPage").asBoolean());
		
		
	}
	
}
