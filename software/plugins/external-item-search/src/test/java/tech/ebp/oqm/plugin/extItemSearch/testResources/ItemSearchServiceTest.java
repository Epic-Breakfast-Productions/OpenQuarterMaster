package tech.ebp.oqm.plugin.extItemSearch.testResources;

import io.smallrye.mutiny.Multi;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.plugin.extItemSearch.model.lookupResult.LookupResult;
import tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.ItemSearchService;
import tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupMethod;
import tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupService;
import tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public abstract class ItemSearchServiceTest<S extends ItemSearchService> extends RunningServerTest {

	protected abstract S getService();

	@ParameterizedTest
	@MethodSource("basicTestParams")
	public void testBasic(
		LookupMethod method,
		LookupSource source,
		String term,
		Integer numFoundExpected,
		Integer numNotFoundExpected
	){
		log.info("Testing service: {}/{}/{}/{}", this.getService().getClass().getSimpleName(), method, source, term);
		Multi<LookupResult> resultMulti = getService().search(
			method,
			source,
			term
		);

		List<LookupResult> results = resultMulti.collect().asList().await().indefinitely();

		log.info("Results: {}", results);

		int numFound = 0;
		int numNotFound = 0;

		for(LookupResult cur : results){
			assertTrue(this.getService().getProviderInfo().getLookupSources().contains(cur.getSource()));

			switch(cur.getType()){
				case SUCCESS -> {
					numFound++;
				}
				case ERROR -> {
					fail("FAILED to call service.");
				}
				case NO_RESULTS -> {
					numNotFound++;
				}
			}
		}

		assertEquals(numFoundExpected, numFound, "Num found not what was expected.");
		assertEquals(numNotFoundExpected, numNotFound, "Num not found not what was expected.");
	}

}
