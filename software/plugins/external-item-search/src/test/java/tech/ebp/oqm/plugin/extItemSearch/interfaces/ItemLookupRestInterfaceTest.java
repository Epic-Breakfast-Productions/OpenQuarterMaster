package tech.ebp.oqm.plugin.extItemSearch.interfaces;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.plugin.extItemSearch.model.SearchType;
import tech.ebp.oqm.plugin.extItemSearch.model.lookupResult.ResultType;
import tech.ebp.oqm.plugin.extItemSearch.testResources.RunningServerTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupSource.BARCODE_LOOKUP;
import static tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupSource.DATAKICK;

@Slf4j
@QuarkusTest
@TestHTTPEndpoint(ItemLookupRestInterface.class)
public class ItemLookupRestInterfaceTest extends RunningServerTest {

	private ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void providersList() {
		String providersList = given()
								   .when().get("info/providers")
								   .then()
								   .statusCode(200)
								   .extract().body().asString();

		log.info("Got providers list: {}", providersList);

		//TODO:: validate
	}

	@Test
	void methodsList() {
		String providersList = given()
								   .when().get("info/methods")
								   .then()
								   .statusCode(200)
								   .extract().body().asString();

		log.info("Got search methods list: {}", providersList);

		//TODO:: validate
	}

	public static Stream<Arguments> getSearches() {
		return Stream.of(
			Arguments.of(
				Map.of(
					"lookupMethod", SearchType.BARCODE,
					"q", "foo"
				),
				List.of(
				)
			),
			Arguments.of(
				Map.of(
					"lookupMethod", SearchType.BARCODE,
					"q", "00888109010058"
				),
				List.of(
					new Result(DATAKICK.name(), ResultType.SUCCESS.name())
				)
			),
			Arguments.of(
				Map.of(
					"lookupMethod", SearchType.BARCODE,
					"q", "886736874135"
				),
				List.of(
					new Result(BARCODE_LOOKUP.name(), ResultType.SUCCESS.name())
				)
			),
			Arguments.of(
				Map.of(
					"lookupMethod", SearchType.TEXT,
					"q", "GPS"
				),
				List.of(
					new Result(BARCODE_LOOKUP.name(), ResultType.SUCCESS.name()),
					new Result(BARCODE_LOOKUP.name(), ResultType.SUCCESS.name()),
					new Result(BARCODE_LOOKUP.name(), ResultType.SUCCESS.name()),
					new Result(BARCODE_LOOKUP.name(), ResultType.SUCCESS.name()),
					new Result(BARCODE_LOOKUP.name(), ResultType.SUCCESS.name()),
					new Result(BARCODE_LOOKUP.name(), ResultType.SUCCESS.name()),
					new Result(BARCODE_LOOKUP.name(), ResultType.SUCCESS.name()),
					new Result(BARCODE_LOOKUP.name(), ResultType.SUCCESS.name()),
					new Result(BARCODE_LOOKUP.name(), ResultType.SUCCESS.name()),
					new Result(BARCODE_LOOKUP.name(), ResultType.SUCCESS.name())
				)
			)
		);
	}

	public record Result(
		String source,
		String type
	)
	{

	}

	@ParameterizedTest
	@MethodSource("getSearches")
	public void searchTest(
		Map<String, ?> parameters,
		List<Result> expected
	) throws JsonProcessingException {
		String searchResultsStr = given()
									  .when()
									  .params(parameters)
									  .get("search")
									  .then()
									  .statusCode(200)
									  .extract().body().asString();
		log.info("Search results: {}", searchResultsStr);

		ArrayNode results = (ArrayNode) objectMapper.readTree(searchResultsStr);

		assertEquals(expected.size(), results.size(), "Wrong number of search results.");

		List<ObjectNode> remaining = new ArrayList<>(results.size());
		for(JsonNode cur : results){
			remaining.add((ObjectNode) cur);
		}

		for (Result curExpected : expected) {
			boolean found = false;
			for (ObjectNode curResult : remaining) {
				if (
					curExpected.source().equals(curResult.get("source").asText())
				) {
					remaining.remove(curResult);
					found = true;
					assertEquals(curExpected.type(), curResult.get("type").asText());
					break;
				}
			}
			assertTrue(found, "Did not find expected result.");
		}
		assertTrue(remaining.isEmpty(), "Failed to find all expected entries.");
	}

}
