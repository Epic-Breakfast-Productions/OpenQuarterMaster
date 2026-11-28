package tech.ebp.oqm.plugin.imageSearch.service.imageSearch.providers.resnet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nu.pattern.OpenCV;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.Result;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Tensor;
import org.tensorflow.ndarray.FloatNdArray;
import org.tensorflow.ndarray.StdArrays;
import org.tensorflow.types.TFloat32;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.sso.KcClientAuthService;
import tech.ebp.oqm.plugin.imageSearch.model.Model;
import tech.ebp.oqm.plugin.imageSearch.model.resnet.ImageVector;
import tech.ebp.oqm.plugin.imageSearch.model.search.ImageFinding;
import tech.ebp.oqm.plugin.imageSearch.model.search.ImageSearch;
import tech.ebp.oqm.plugin.imageSearch.model.search.SearchResults;
import tech.ebp.oqm.plugin.imageSearch.service.ImageToItemCache;
import tech.ebp.oqm.plugin.imageSearch.service.imageSearch.ImageSearchService;
import tech.ebp.oqm.plugin.imageSearch.service.imageSearch.providers.ImageSearchProvider;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;

@Slf4j
@ApplicationScoped
public class ResnetProvider extends ImageSearchProvider {

	@Getter(AccessLevel.PRIVATE)
	@Inject
	ResnetDataProcessor resnetDataProcessor;

	@Inject
	ResnetVectorService resnetVectorService;

	@Inject
	ImageToItemCache imageToItemCache;

	@RestClient
	OqmCoreApiClientService oqmCoreApiClientService;

	@Inject
	KcClientAuthService serviceAccountService;

	@Getter(AccessLevel.PROTECTED)
	Instant lastProcess;

	@Getter(AccessLevel.PROTECTED)
	boolean initializing = false;

	@Override
	public boolean isProcessing() {
		boolean processing = this.getLastProcess() != null &&
			Duration.between(this.getLastProcess(), Instant.now()).getSeconds() < 5;

		return processing || this.isInitializing();
	}


	public void processImage(String database, String imageId, int imageRevision) {
		log.info("Processing revision {} of image {}", imageRevision, imageId);
		this.lastProcess = Instant.now();

		//check if already processed, skip if exists
		if (this.resnetVectorService.vectorExists(imageId, imageRevision)) {
			log.info("Image {} already processed, skipping.", imageId);
			return;
		}

		try (
			InputStream is = (InputStream) this.oqmCoreApiClientService.imageGetRevisionData(this.serviceAccountService.getAuthString(), database, imageId, imageRevision + "")
				.await()
				.indefinitely()
				.getEntity()
		) {
			ImageVector.ImageVectorBuilder builder = ImageVector.builder();

			builder.oqmDb(database);
			builder.imageId(imageId);
			builder.imageRevision(imageRevision);
			builder.vector(this.resnetDataProcessor.generateImageFeatureVector(is));

			this.resnetVectorService.addVector(builder.build());
		} catch (IOException e) {
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

	public void processImage(String oqmDatabase, String imageId) {
		this.processImage(
			oqmDatabase,
			this.oqmCoreApiClientService.imageGet(this.serviceAccountService.getAuthString(), oqmDatabase, imageId).await().indefinitely()
		);
	}

	public synchronized long cullDeleted() {
		log.info("Culling images deleted in Core API.");
		long output = 0;
		Iterator<ImageVector> allVec = this.resnetVectorService.getAllVectors();
		while (allVec.hasNext()) {
			ImageVector imageVector = allVec.next();
			String imageID = imageVector.getImageId();
			String db = imageVector.getOqmDb();
			// TODO:: check which error is being thrown, ensure dbdeleted exception
			ObjectNode imageData = this.oqmCoreApiClientService
				.imageGet(
					this.serviceAccountService.getAuthString(), db, imageID)
				.onFailure()
				.recoverWithNull()
				.await().indefinitely();
			if (imageData == null) {
				this.resnetVectorService.deleteVector(imageVector);
				output++;
			}
		}
		log.info("Done culling images deleted in Core API.");
		return output;
	}

	//Iterates db, operate on each image in turn
	@WithSpan
	public void initVectors() {
		this.initializing = true;
		log.info("Processing images in all databases.");
		try {
			tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.ImageSearch imageSearch = tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.ImageSearch.builder()
				.pageNum(1)
				.pageSize(100)
				.build();

			ArrayNode dbList = this.oqmCoreApiClientService.manageDbList(this.serviceAccountService.getAuthString()).await().indefinitely();
			for (JsonNode db : dbList) {

				String curOqmDb = db.get("name").asText(); //circle back to
				ObjectNode results;

				log.info("Processing images database: {}", curOqmDb);

				try {
					do {
						results = this.oqmCoreApiClientService.imageSearch(this.serviceAccountService.getAuthString(), curOqmDb, imageSearch).await().indefinitely();

						log.debug("Retrieved new page of results");

						for (JsonNode curImageResult : results.get("results")) {
							this.processImage(curOqmDb, (ObjectNode) curImageResult);
						}

						imageSearch.setPageNum(imageSearch.getPageNum() + 1);
					} while (!results.get("pagingCalculations").get("onLastPage").asBoolean());

				} catch (Exception e) {
					log.error("Error processing images database: {}", curOqmDb, e);
					throw new RuntimeException(e);
				}
			}

			//remove vectors not in oqm core db
			this.cullDeleted();
		} finally {
			this.initializing = false;
		}
	}


	@Override
	public void search(ImageSearch query, SearchResults output) {
		log.info("Getting similarities for query");

		float[] queryFeatures;
		try {
			queryFeatures = this.getResnetDataProcessor().generateImageFeatureVector(query.file);
		} catch (IOException e) {
			throw new RuntimeException("Failed to get features for input file.", e);
		}

		for (Iterator<ImageVector> it = this.resnetVectorService.getAllVectors(query.oqmDbIdOrName); it.hasNext(); ) {
			ImageVector curData = it.next();

			log.trace("Processing image comparison with image: {}", curData.getImageId());
			double simScore = this.getResnetDataProcessor().cosineSimilarity(queryFeatures, curData.getVector());

			if (simScore < query.threshold) {
				continue;
			}

			for (String curItemId : this.imageToItemCache.getItemsForImage(query.oqmDbIdOrName, curData.getImageId())) {
				output.add(
					ImageFinding.builder()
						.model(Model.RESNET_v2)
						.itemId(curItemId)
						.imageId(curData.getImageId())
						.score(simScore)
						.build()
				);
			}

			log.trace("Done processing image comparison with image: {}", curData.getImageId());
		}

		log.info("Done getting similarities for query.");
	}
}
