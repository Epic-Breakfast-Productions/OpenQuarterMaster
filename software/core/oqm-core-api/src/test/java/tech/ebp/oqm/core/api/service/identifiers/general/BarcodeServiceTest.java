package tech.ebp.oqm.core.api.service.identifiers.general;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.GeneralId;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.Generic;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.ean.EAN_13;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.ean.EAN_8;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.gtin.GTIN_14;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.isbn.ISBN_10;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.isbn.ISBN_13;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.upc.UPC_A;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.upc.UPC_E;
import tech.ebp.oqm.core.api.testResources.lifecycleManagers.TestResourceLifecycleManager;
import tech.ebp.oqm.core.api.testResources.testClasses.RunningServerTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@QuarkusTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
class BarcodeServiceTest extends CodeUtilTestBase {
	
	@Inject BarcodeService barcodeService;
	
	@ParameterizedTest
	@MethodSource("getCodes")
	public void isValidUPCACodeValid(String code, GeneralId identifier) {
		String data = barcodeService.getGeneralIdData(identifier);
		
		Path outputPath =
			Path.of("build/test-results/" + ConfigProvider.getConfig().getValue("quarkus.profile", String.class) + "/barcodes/" + identifier.getType() + "-" + identifier.getValue() + ".svg");
		
		try{
			outputPath.toFile().getParentFile().mkdirs();
			Files.writeString(outputPath, data, StandardOpenOption.CREATE_NEW);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}