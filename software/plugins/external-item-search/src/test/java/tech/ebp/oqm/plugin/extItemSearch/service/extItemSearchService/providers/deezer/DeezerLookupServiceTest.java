package tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.providers.deezer;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.provider.Arguments;
import tech.ebp.oqm.plugin.extItemSearch.testResources.ItemSearchServiceTest;

import java.util.stream.Stream;

import static tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupMethod.MUSIC_ALBUM;
import static tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupSource.DEEZER;

@QuarkusTest
@Slf4j
class DeezerLookupServiceTest extends ItemSearchServiceTest<DeezerLookupService> {
	@Getter
	@Inject
	DeezerLookupService service;

	public static Stream<Arguments> basicTestParams() {
		return Stream.of(
			Arguments.of(MUSIC_ALBUM, DEEZER, "twenty one pilots more than we ever imagined", 1, 0),
			Arguments.of(MUSIC_ALBUM, DEEZER, "no-such-album", 0, 1)
		);
	}
}