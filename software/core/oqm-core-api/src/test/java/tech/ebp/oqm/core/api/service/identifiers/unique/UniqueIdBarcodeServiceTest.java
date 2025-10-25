package tech.ebp.oqm.core.api.service.identifiers.unique;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.unique.ProvidedUniqueId;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.unique.UniqueId;
import tech.ebp.oqm.core.api.testResources.lifecycleManagers.TestResourceLifecycleManager;
import tech.ebp.oqm.core.api.testResources.testClasses.WebServerTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;

@Slf4j
@QuarkusTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
class UniqueIdBarcodeServiceTest extends WebServerTest {
	
	private static void writeToFile(String data, String dir, String fileName) {
		Path outputPath = Path.of(
			"build/test-results/" +
			ConfigProvider.getConfig().getValue("quarkus.profile", String.class) +
			"/barcodes/unique/" + dir + "/" +
			fileName + ".svg"
		);
		
		try{
			outputPath.toFile().getParentFile().mkdirs();
			Files.writeString(outputPath, data, StandardOpenOption.CREATE);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Inject
	UniqueIdBarcodeService uniqueIdBarcodeService;
	
	@Test
	public void objectIdBarcodeGenTest() {
		ObjectId identifier = new ObjectId();
		
		StopWatch sw = StopWatch.createStarted();
		String data = uniqueIdBarcodeService.getObjectIdData(identifier);
		sw.stop();
		log.info("Generated barcode for ObjectId in {}", sw);
		
		writeToFile(data, "objectId", identifier.toHexString());
	}
	
	public static Stream<Arguments> uniqueIds() {
		return Stream.of(
			Arguments.of(ProvidedUniqueId.builder()
							 .barcode(true)
							 .value(FAKER.idNumber().ssnValid())
							 .label(FAKER.name().name())
							 .build())
		);
	}
	
	@ParameterizedTest
	@MethodSource("uniqueIds")
	public void uniqueIdBarcodeGenTest(UniqueId identifier) {
		
		StopWatch sw = StopWatch.createStarted();
		String data = uniqueIdBarcodeService.getUniqueIdData(identifier);
		sw.stop();
		log.info("Generated barcode for unique Id {} in {}", identifier, sw);
		
		writeToFile(data, "uniqueIds", identifier.getValue());
	}
	
	

}