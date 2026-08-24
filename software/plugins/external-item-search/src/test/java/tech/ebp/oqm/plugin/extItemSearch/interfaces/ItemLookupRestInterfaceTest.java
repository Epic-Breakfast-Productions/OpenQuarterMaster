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
import tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupMethod;
import tech.ebp.oqm.plugin.extItemSearch.testResources.RunningServerTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupSource.*;

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
				new ArrayList<>(10){{
					for(int i = 0; i < 10; i++){add(new Result(BARCODE_LOOKUP.name(), ResultType.SUCCESS.name()));}
				}}
			),
			Arguments.of(
				Map.of(
					"lookupMethod", SearchType.BARCODE,
					"q", "3046920029759"
				),
				List.of(
					new Result(OPENFOODFACTS.name(), ResultType.SUCCESS.name())
				)
			),
			Arguments.of(
				Map.of(
					"lookupMethod", SearchType.TEXT,
					"q", "beshbarmak"
				),
				List.of(
					new Result(OPENFOODFACTS.name(), ResultType.SUCCESS.name())
				)
			),
			Arguments.of(
				Map.of(
					"lookupMethod", SearchType.BARCODE,
					"q", "017078987621"
				),
				List.of(
					new Result(UPC_ITEM_DB.name(), ResultType.SUCCESS.name())
				)
			),
			Arguments.of(
				Map.of(
					"lookupMethod", LookupMethod.PART_NUM,
					"q", "012345"
				),
				List.of(
					new Result(REBRICKABLE.name(), ResultType.SUCCESS.name())
				)
			),
			Arguments.of(
				Map.of(
					"lookupMethod", LookupMethod.TEXT,
					"q", "2x2 brick"
				),
				new ArrayList<>(100){{
					for(int i = 0; i < 100; i++){add(new Result(REBRICKABLE.name(), ResultType.SUCCESS.name()));}
				}}
			),
			Arguments.of(
				Map.of(
					"lookupMethod", LookupMethod.SET_NUM,
					"q", "21309-1"
				),
				List.of(
					new Result(REBRICKABLE.name(), ResultType.SUCCESS.name())
				)
			),
			Arguments.of(
				Map.of(
					"lookupMethod", LookupMethod.TEXT,
					"q", "Saturn V"
				),
				new ArrayList<>(3){{
					for(int i = 0; i < 3; i++){add(new Result(REBRICKABLE.name(), ResultType.SUCCESS.name()));}
				}}
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
