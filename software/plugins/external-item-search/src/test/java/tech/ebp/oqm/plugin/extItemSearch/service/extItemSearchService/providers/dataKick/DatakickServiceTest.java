package tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.providers.dataKick;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;
import tech.ebp.oqm.plugin.extItemSearch.testResources.ItemSearchServiceTest;
import tech.ebp.oqm.plugin.extItemSearch.testResources.RunningServerTest;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupMethod.BARCODE;
import static tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupSource.BARCODE_LOOKUP;
import static tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupSource.DATAKICK;

@QuarkusTest
@Slf4j
public class DatakickServiceTest extends ItemSearchServiceTest<DatakickService> {

	@Getter
	@Inject
	DatakickService service;

	public static Stream<Arguments> basicTestParams(){
		return Stream.of(
			Arguments.of(BARCODE, DATAKICK, "00888109010058", 1, 0),
			Arguments.of(BARCODE, DATAKICK, "foo", 0, 1)
		);
	}
}
