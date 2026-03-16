package tech.ebp.oqm.lib.core.api.java.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.lib.core.api.java.search.QParamVal;
import tech.ebp.oqm.lib.core.api.java.search.QueryParams;

import java.net.URI;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class UriUtilsTest {
	
	public static Stream<Arguments> urlBuilderParams() {
		return Stream.of(
			Arguments.of(
				URI.create("https://localhost:8080/"),
				URI.create("https://localhost:8080/"), "", new QueryParams()
			),
			Arguments.of(
				URI.create("https://localhost:8080/foo"),
				URI.create("https://localhost:8080/"), "foo", new QueryParams()
			),
			Arguments.of(
				URI.create("https://localhost:8080/foo?foo=bar"),
				URI.create("https://localhost:8080/"), "foo", new QueryParams(){{
					this.put("foo", QParamVal.of("bar"));
				}}
			),
			Arguments.of(
				URI.create("https://localhost:8080/foo?foo=bar&foo2=bar2"),
				URI.create("https://localhost:8080/"), "foo", new QueryParams(){{
					this.put("foo", QParamVal.of("bar"));
					this.put("foo2", QParamVal.of("bar2"));
				}}
			),
			Arguments.of(
				URI.create("https://localhost:8080/foo?foo=bar&foo=bar2"),
				URI.create("https://localhost:8080/"), "foo", new QueryParams(){{
					this.put("foo", QParamVal.of("bar", "bar2"));
				}}
			),
			Arguments.of(
				URI.create("https://localhost:8080/core/api"),
				URI.create("https://localhost:8080/core/api"), "", new QueryParams()
			),
			Arguments.of(
				URI.create("https://localhost:8080/core/api/q/health"),
				URI.create("https://localhost:8080/core/api"), "/q/health", new QueryParams()
			),
			Arguments.of(
				URI.create("https://localhost/core/api/q/health"),
				URI.create("https://localhost/core/api"), "/q/health", new QueryParams()
			)
		);
	}
	
	@ParameterizedTest
	@MethodSource("urlBuilderParams")
	public void testUrlBuilder(URI expected, URI baseUri, String path, QueryParams query) {
		URI result = UriUtils.buildUri(
			baseUri,
			path,
			query
		);
		
		assertEquals(expected, result);
	}
	

}