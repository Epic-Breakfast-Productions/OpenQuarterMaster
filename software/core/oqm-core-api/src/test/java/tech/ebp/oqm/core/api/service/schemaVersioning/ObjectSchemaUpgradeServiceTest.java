package tech.ebp.oqm.core.api.service.schemaVersioning;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.InsertOneResult;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.core.api.config.CoreApiInteractingEntity;
import tech.ebp.oqm.core.api.model.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.core.api.model.object.upgrade.TotalUpgradeResult;
import tech.ebp.oqm.core.api.service.mongo.InteractingEntityService;
import tech.ebp.oqm.core.api.service.mongo.InventoryItemService;
import tech.ebp.oqm.core.api.service.mongo.ItemCheckoutService;
import tech.ebp.oqm.core.api.service.mongo.MongoService;
import tech.ebp.oqm.core.api.service.mongo.StorageBlockService;
import tech.ebp.oqm.core.api.service.mongo.StoredService;
import tech.ebp.oqm.core.api.testResources.testClasses.RunningServerTest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static tech.ebp.oqm.core.api.testResources.TestConstants.DEFAULT_TEST_DB_NAME;

/**
 * TODO's:
 *  -
 */
@Disabled("Breaking, needs rework, not relevant in current meta")
@Slf4j
@QuarkusTest
public class ObjectSchemaUpgradeServiceTest extends RunningServerTest {
	
	@Inject
	InteractingEntityService interactingEntityService;
	
	@Inject
	StorageBlockService storageBlockService;
	
	@Inject
	InventoryItemService inventoryItemService;
	
	@Inject
	StoredService storedService;
	
	@Inject
	ItemCheckoutService itemCheckoutService;
	
	@Inject
	CoreApiInteractingEntity coreApiInteractingEntity;
	
	@Inject
	ObjectSchemaUpgradeService objectSchemaUpgradeService;
	
	public static Stream<Arguments> tests() throws IOException {
		List<Arguments> tests = new ArrayList<>();
		Path upgradingDir = Path.of(ObjectSchemaUpgradeServiceTest.class.getResource("/upgrading/").getPath());
		
		log.info("Pulling test cases from upgrading dir: {}", upgradingDir);
		
		return Files.list(upgradingDir)
				   .filter(Files::isDirectory)
				   .sorted()
				   .map(Arguments::of);
	}
	
	private void createExistingObjects(ObjectNode existingObjects) {
		for (JsonNode storageBlock : (ArrayNode) existingObjects.get("StorageBlock")) {
			String id = storageBlock.asText();
			
			this.storageBlockService.add(
				DEFAULT_TEST_DB_NAME,
				StorageBlock.builder()
					.id(new ObjectId(id))
					.label(FAKER.location().building())
					.build(),
				this.coreApiInteractingEntity
			);
		}
	}
	
	protected long loadDocuments(Path entriesDir, MongoCollection<Document> docCollection) throws IOException {
		AtomicLong countExpected = new AtomicLong();
		
		if (Files.exists(entriesDir)) {
			log.info("Loading old objects into collection from {}", entriesDir);
			
			try (Stream<Path> files = Files.list(entriesDir)) {
				files.filter(Files::isRegularFile)
					.filter((path)->path.getFileName().toString().endsWith(".json"))
					.map(Path::toUri)
					.map(file->{
						try {
							return IOUtils.toString(file, StandardCharsets.UTF_8);
						} catch(IOException e) {
							throw new RuntimeException(e);
						}
					})
					.map(Document::parse)
					.forEach(oldObj->{
						countExpected.getAndIncrement();
						log.info("Inserting old object: {}", oldObj);
						InsertOneResult result = docCollection.insertOne(oldObj);
						
						result.wasAcknowledged();
					});
			}
			log.info("Created {} old objects.", docCollection.countDocuments());
		}
		return countExpected.get();
	}
	
	@ParameterizedTest
	@MethodSource("tests")
	public void testSchemaService(Path caseDir) throws IOException {
		log.info("Running test with case directory: {}", caseDir);
		ObjectNode caseDetails = (ObjectNode) OBJECT_MAPPER.readTree(caseDir.resolve("case.json").toFile());
		log.debug("Case details: {}", caseDetails);
		
		this.createExistingObjects((ObjectNode) caseDetails.get("existing"));
		
		long intEntCountExpected = this.loadDocuments(caseDir.resolve("InteractingEntity"), this.interactingEntityService.getDocumentCollection());
		long historyEventCountExpected = this.loadDocuments(caseDir.resolve("HistoryEvent"), this.inventoryItemService.getHistoryService().getDocumentCollection(DEFAULT_TEST_DB_NAME));
		long invItemCountExpected = this.loadDocuments(caseDir.resolve("InventoryItem"), this.inventoryItemService.getDocumentCollection(DEFAULT_TEST_DB_NAME));
		long itemCheckoutCountExpected = this.loadDocuments(caseDir.resolve("ItemCheckout"), this.itemCheckoutService.getDocumentCollection(DEFAULT_TEST_DB_NAME));
		long storedExpected = this.loadDocuments(caseDir.resolve("Stored"), this.storedService.getDocumentCollection(DEFAULT_TEST_DB_NAME));
		
		log.info("Performing upgrade.");
		Optional<TotalUpgradeResult> resultOptional = this.objectSchemaUpgradeService.updateSchema(true);
		log.info("DONE Performing upgrade.");
		
		assertTrue(resultOptional.isPresent());
		TotalUpgradeResult result = resultOptional.get();
		log.info("Upgrade result: {}", result);
	}
}
