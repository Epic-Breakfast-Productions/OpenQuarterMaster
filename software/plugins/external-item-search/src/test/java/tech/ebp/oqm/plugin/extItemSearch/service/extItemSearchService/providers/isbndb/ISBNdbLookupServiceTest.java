package tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.providers.isbndb;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.provider.Arguments;
import tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.providers.barcodeLookup.BarcodeLookupService;
import tech.ebp.oqm.plugin.extItemSearch.testResources.ItemSearchServiceTest;

import java.util.stream.Stream;

import static tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupMethod.BARCODE;
import static tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupMethod.TEXT;
import static tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupSource.ISBNDB;

@QuarkusTest
@Slf4j
class ISBNdbLookupServiceTest extends ItemSearchServiceTest<ISBNdbLookupService> {

	@Getter
	@Inject
	ISBNdbLookupService service;

	public static Stream<Arguments> basicTestParams(){
		return Stream.of(
			Arguments.of(BARCODE, ISBNDB, "1603632431", 1, 0),
			Arguments.of(BARCODE, ISBNDB, "foo", 0, 1),

			Arguments.of(TEXT, ISBNDB, "Biology", 1, 0),
			Arguments.of(TEXT, ISBNDB, "foo", 0, 1)
		);
	}
}