package tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.providers.upcItemDb;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.provider.Arguments;
import tech.ebp.oqm.plugin.extItemSearch.testResources.ItemSearchServiceTest;

import java.util.stream.Stream;

import static tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupMethod.BARCODE;
import static tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupSource.UPC_ITEM_DB;

@QuarkusTest
@Slf4j
class UpcItemDbServiceTest extends ItemSearchServiceTest<UpcItemDbService> {

	@Getter
	@Inject
	UpcItemDbService service;

	public static Stream<Arguments> basicTestParams(){
		return Stream.of(
			Arguments.of(BARCODE, UPC_ITEM_DB, "017078987621", 1, 0),
			Arguments.of(BARCODE, UPC_ITEM_DB, "foo", 0, 1)
		);
	}
}