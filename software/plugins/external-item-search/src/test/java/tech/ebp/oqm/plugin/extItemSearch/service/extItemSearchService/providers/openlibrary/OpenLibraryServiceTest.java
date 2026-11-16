package tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.providers.openlibrary;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.provider.Arguments;
import tech.ebp.oqm.plugin.extItemSearch.testResources.ItemSearchServiceTest;

import java.util.stream.Stream;

import static tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupMethod.BARCODE;
import static tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupMethod.TEXT;
import static tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupSource.OPENLIBRARY;


@QuarkusTest
@Slf4j
class OpenLibraryServiceTest extends ItemSearchServiceTest<OpenLibraryService>  {

	@Getter
	@Inject
	OpenLibraryService service;

	public static Stream<Arguments> basicTestParams(){
		return Stream.of(
			Arguments.of(BARCODE, OPENLIBRARY, "1501752014", 1, 0),
			Arguments.of(BARCODE, OPENLIBRARY, "foo", 0, 1),

			Arguments.of(TEXT, OPENLIBRARY, "Alash", 10, 0),
			Arguments.of(TEXT, OPENLIBRARY, "foo", 0, 1)
		);
	}
}