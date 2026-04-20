package tech.ebp.oqm.core.api.service.mongo.search;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.core.api.service.mongo.search.PagingOptions;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
class PagingOptionsTest {
	
	public static Stream<Arguments> getFromArgs() {
		return Stream.of(
			Arguments.of(1, 1, true, 1, 1, 0, null),
			Arguments.of(1, 1, true, 1, 1, 0, null),
			Arguments.of(null, 1, false, PagingOptions.DEFAULT_PAGE_SIZE, 1, 0, null),
			Arguments.of(1, null, true, 1, PagingOptions.DEFAULT_PAGE_NUM, 0, null),
			Arguments.of(null, null, false, Integer.MAX_VALUE, 1, 0, null),
			Arguments.of(-1, 1, true, 1, 1, 0, IllegalArgumentException.class),
			Arguments.of(1, -1, true, 1, 1, 0, IllegalArgumentException.class)
		);
	}
	
	@ParameterizedTest(name = "testFromQueryParams[{index}]")
	@MethodSource("getFromArgs")
	public void testFromQueryParams(
		Integer pageSize,
		Integer pageNum,
		boolean expectedDoPaging,
		int expectedPageSize,
		int expectedPageNum,
		int expectedSkipVal,
		Class<Throwable> expectedE
	) {
		if (expectedE == null) {
			PagingOptions ops = PagingOptions.from(pageSize, pageNum);
			
			assertEquals(expectedDoPaging, ops.isDoPaging());
			assertEquals(expectedPageSize, ops.getPageSize());
			assertEquals(expectedPageNum, ops.getPageNum());
			assertEquals(expectedSkipVal, ops.getSkipVal());
		} else {
			assertThrows(
				expectedE,
				()->{
					PagingOptions.from(pageSize, pageNum);
				}
			);
		}
	}
}