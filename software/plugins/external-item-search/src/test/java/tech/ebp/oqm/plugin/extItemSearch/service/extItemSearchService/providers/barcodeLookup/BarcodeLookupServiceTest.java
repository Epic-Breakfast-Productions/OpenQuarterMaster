package tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.providers.barcodeLookup;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.provider.Arguments;
import tech.ebp.oqm.plugin.extItemSearch.testResources.ItemSearchServiceTest;

import java.util.stream.Stream;

import static tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupMethod.BARCODE;
import static tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupMethod.TEXT;
import static tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupSource.BARCODE_LOOKUP;

@QuarkusTest
@Slf4j
class BarcodeLookupServiceTest extends ItemSearchServiceTest<BarcodeLookupService> {

	@Getter
	@Inject
	BarcodeLookupService service;

	public static Stream<Arguments> basicTestParams(){
		return Stream.of(
			Arguments.of(BARCODE, BARCODE_LOOKUP, "886736874135", 1, 0),
			Arguments.of(BARCODE, BARCODE_LOOKUP, "foo", 0, 1),

			Arguments.of(TEXT, BARCODE_LOOKUP, "GPS", 10, 0),
			Arguments.of(TEXT, BARCODE_LOOKUP, "foo", 0, 1)
		);
	}

}