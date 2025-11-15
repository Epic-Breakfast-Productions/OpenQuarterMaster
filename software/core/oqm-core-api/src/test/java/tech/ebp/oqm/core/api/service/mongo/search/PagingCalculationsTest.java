package tech.ebp.oqm.core.api.service.mongo.search;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.core.api.service.mongo.search.PagingCalculations;
import tech.ebp.oqm.core.api.service.mongo.search.PagingOptions;
import tech.ebp.oqm.core.api.service.mongo.search.SearchResult;
import tech.ebp.oqm.core.api.testResources.data.TestMainObject;
import tech.ebp.oqm.core.api.testResources.data.TestMainObjectSearch;

import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class PagingCalculationsTest {
	
	public static Stream<Arguments> getConstructorArgs() {
		return Stream.of(
			Arguments.of(new PagingOptions(1, 1), 1, true, true, 1, 1, 1, 1),
			Arguments.of(new PagingOptions(1, 1), 3, true, false, 3, 1, 2, 1),
			Arguments.of(new PagingOptions(1, 2), 3, false, false, 3, 2, 3, 1),
			Arguments.of(new PagingOptions(1, 3), 3, false, true, 3, 3, 3, 2)
		);
	}
	
	public static Stream<Arguments> getPageIteratorArgs() {
		return Stream.of(
			Arguments.of(new PagingCalculations(new PagingOptions(1, 1), 3), 3)
		);
	}
	
	
	@ParameterizedTest(name = "testConstructor[{index}]")
	@MethodSource("getConstructorArgs")
	public void testConstructor(
		PagingOptions options,
		long numResults,
		boolean expectedOnFirstPage,
		boolean expectedOnLastPage,
		long expectedNumPages,
		long expectedCurPage,
		long expectedNextPage,
		long expectedPreviousPage
	) {
		PagingCalculations calculations = new PagingCalculations(options, numResults);
		
		assertEquals(expectedOnFirstPage, calculations.isOnFirstPage());
		assertEquals(expectedOnLastPage, calculations.isOnLastPage());
		assertEquals(expectedNumPages, calculations.getNumPages());
		assertEquals(expectedNumPages, calculations.getLastPage());
		assertEquals(expectedCurPage, calculations.getCurPage());
		assertEquals(expectedNextPage, calculations.getNextPage());
		assertEquals(expectedPreviousPage, calculations.getPreviousPage());
	}
	
	@ParameterizedTest(name = "testPageIterator[{index}]")
	@MethodSource("getPageIteratorArgs")
	public void testPageIterator(
		PagingCalculations calculations,
		long expectedNumPages
	) {
		
		long curPage = 0;
		
		Iterator<Long> it = calculations.getPageIterator();
		
		while (it.hasNext()) {
			long gotten = it.next();
			
			assertEquals(++curPage, gotten);
		}
		
		assertEquals(curPage, expectedNumPages);
	}
	
	@Test
	public void testOnPage() {
		PagingCalculations calculations = new PagingCalculations(new PagingOptions(1, 2), 3);
		
		assertFalse(calculations.onPage(1));
		assertTrue(calculations.onPage(2));
		assertFalse(calculations.onPage(3));
	}
	
	@Test
	public void testSearchConstructor() {
		PagingCalculations calculations = new PagingCalculations(
			new SearchResult<TestMainObject>(Collections.emptyList(), 3, false, new PagingOptions(1, 2), new TestMainObjectSearch())
		);
		
		assertEquals(3, calculations.getNumPages());
	}
}