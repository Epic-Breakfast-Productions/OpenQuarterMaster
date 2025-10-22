package tech.ebp.oqm.core.api.service.identifiers.general;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.GeneralId;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@QuarkusTest
class BarcodeServiceTest extends CodeUtilTestBase {
	
	private static void writeToFile(String data, String dir, String fileName) {
		Path outputPath = Path.of(
			"build/test-results/" +
			ConfigProvider.getConfig().getValue("quarkus.profile", String.class) +
			"/barcodes/" + dir + "/" +
			fileName + ".svg"
		);
		
		try{
			outputPath.toFile().getParentFile().mkdirs();
			Files.writeString(outputPath, data, StandardOpenOption.CREATE);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Inject BarcodeService barcodeService;
	
	@ParameterizedTest
	@MethodSource("getCodes")
	public void generalIdBarcodeGenTest(String code, GeneralId identifier) {
		StopWatch sw = StopWatch.createStarted();
		String data = barcodeService.getGeneralIdData(identifier);
		sw.stop();
		log.info("Generated barcode for {} in {}", identifier, sw);
		
		assertNotNull(data);
		assertFalse(data.isEmpty());
		
		writeToFile(data, "generalIds", identifier.getType() + "-" + identifier.getValue());
	}
	
	@Test
	public void objectIdBarcodeGenTest() {
		ObjectId identifier = new ObjectId();
		
		StopWatch sw = StopWatch.createStarted();
		String data = barcodeService.getObjectIdData(identifier);
		sw.stop();
		log.info("Generated barcode for ObjectId in {}", sw);
		
		writeToFile(data, "objectId", identifier.toHexString());
	}
	
}