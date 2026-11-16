package tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.providers.rebrickable;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.provider.Arguments;
import tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.providers.barcodeLookup.BarcodeLookupService;
import tech.ebp.oqm.plugin.extItemSearch.testResources.ItemSearchServiceTest;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupMethod.*;
import static tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupSource.BARCODE_LOOKUP;
import static tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupSource.REBRICKABLE;

@QuarkusTest
@Slf4j
class RebrickableServiceTest extends ItemSearchServiceTest<RebrickableService> {

	@Getter
	@Inject
	RebrickableService service;

	public static Stream<Arguments> basicTestParams(){
		return Stream.of(
			Arguments.of(PART_NUM, REBRICKABLE, "012345", 1, 0),
			Arguments.of(PART_NUM, REBRICKABLE, "foo", 0, 1),

			Arguments.of(SET_NUM, REBRICKABLE, "21309-1", 1, 0),
			Arguments.of(SET_NUM, REBRICKABLE, "foo", 0, 1),

			Arguments.of(TEXT, REBRICKABLE, "2x2 brick", 100, 1),
			Arguments.of(TEXT, REBRICKABLE, "Saturn V", 3, 1),
			Arguments.of(TEXT, REBRICKABLE, "foo", 0, 2)
		);
	}
}