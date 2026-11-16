package tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.providers.openfoodfacts;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.provider.Arguments;
import tech.ebp.oqm.plugin.extItemSearch.testResources.ItemSearchServiceTest;

import java.util.stream.Stream;

import static tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupMethod.BARCODE;
import static tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupMethod.TEXT;
import static tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupSource.OPENFOODFACTS;

@QuarkusTest
@Slf4j
class OpenFoodFactsServiceTest extends ItemSearchServiceTest<OpenFoodFactsService> {

	@Getter
	@Inject
	OpenFoodFactsService service;

	public static Stream<Arguments> basicTestParams(){
		return Stream.of(
			Arguments.of(BARCODE, OPENFOODFACTS, "3046920029759", 1, 0),
			Arguments.of(BARCODE, OPENFOODFACTS, "foo", 0, 1),

			Arguments.of(TEXT, OPENFOODFACTS, "beshbarmak", 1, 0),
			Arguments.of(TEXT, OPENFOODFACTS, "foo", 0, 1)
		);
	}
}