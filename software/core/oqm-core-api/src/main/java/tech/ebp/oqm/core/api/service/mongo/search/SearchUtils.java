package tech.ebp.oqm.core.api.service.mongo.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import tech.ebp.oqm.core.api.model.object.ObjectUtils;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import javax.measure.Unit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.*;

@Slf4j
public class SearchUtils {
	
	private static final String ANY_NONE_OR_MANY_CHARS = "[\\s\\S]*";
	
	public static Pattern getSearchTermPattern(String term) {
		return Pattern.compile(
			//TODO:: quoting seems to break searching for sanitized characters (but quoting required to not throw exceptions)
			ANY_NONE_OR_MANY_CHARS + Pattern.quote(term) + ANY_NONE_OR_MANY_CHARS,
			Pattern.CASE_INSENSITIVE
		);
	}
	
	public static Bson getBasicSearchFilter(String field, String value) {
		if (value != null && !value.isBlank()) {
			return regex(
				field,
				SearchUtils.getSearchTermPattern(value.strip())
			);
		}
		return null;
	}
	
	public static void addBasicSearchFilter(List<Bson> filters, String field, String value) {
		Bson filter = getBasicSearchFilter(field, value);
		if(filter != null){
			filters.add(filter);
		}
	}
	
	public static void addKeywordSearchFilter(List<Bson> filters, List<String> keywords) {
		if (keywords != null) {
			for (String keyword : keywords) {
				filters.add(in("keywords", keyword));
			}
		}
	}
	
	public static void addAttributeSearchFilters(List<Bson> filters, Map<String, String> attributes) {
		if (attributes != null) {
			for (Map.Entry<String, String> curAtt : attributes.entrySet()) {
				Bson inFilter = exists("attributes." + curAtt.getKey());
				
				if (curAtt.getValue() == null || curAtt.getValue().isBlank()) {
					filters.add(inFilter);
				} else {
					filters.add(and(
						inFilter,
						eq("attributes." + curAtt.getKey(), curAtt.getValue())
					));
				}
			}
		}
	}
	
	public static Map<String, String> attListsToMap(List<String> attributeKeys, List<String> attributeValues) {
		if (attributeKeys == null || attributeValues == null) {
			if (attributeKeys != attributeValues) {
				throw new IllegalArgumentException("Attribute key/ value lists must both exist.");
			}
			return null;
		}
		if (attributeKeys.size() != attributeValues.size()) {
			throw new IllegalArgumentException("Attribute key/ value lists must both be of the same size.");
		}
		Map<String, String> output = new HashMap<>();
		
		for (int i = 0; i < attributeKeys.size(); i++) {
			output.put(attributeKeys.get(i), attributeValues.get(i));
		}
		
		return output;
	}
	
	public static List<Quantity<?>> capacityListsToMap(List<Integer> capacities, List<String> units) {
		if (capacities == null || units == null) {
			if (capacities != (Object) units) {
				throw new IllegalArgumentException("Capacity/ unit lists must both exist.");
			}
			return null;
		}
		if (capacities.size() != units.size()) {
			throw new IllegalArgumentException("Capacity/ unit lists must both be of the same size.");
		}
		List<Quantity<?>> output = new ArrayList<>(capacities.size());
		
		for (int i = 0; i < capacities.size(); i++) {
			try {
				output.add(Quantities.getQuantity(
					capacities.get(i),
					(Unit<?>) ObjectUtils.OBJECT_MAPPER.readValue(units.get(i), Unit.class)
				));
			} catch(JsonProcessingException e) {
				throw new IllegalArgumentException("Unable to parse unit: \"" + units.get(i) + "\"", e);
			}
		}
		
		return output;
	}
	
	/**
	 * TODO:: test
	 *
	 * @param field
	 * @param sortType
	 *
	 * @return The sort bson filter. Null if field is null or blank. Ascending by default.
	 */
	public static Bson getSortBson(String field, SortType sortType) {
		if (field == null || field.isBlank()) {
			return null;
		}
		
		switch (sortType) {
			case DESCENDING:
				return Sorts.descending(field);
			case ASCENDING:
			default:
				return Sorts.ascending(field);
		}
	}
	
	
	public static Bson getKeywordBson(List<String> keywords) {
		return in("keywords", keywords);
	}
	
	/**
	 * Parses attribute searches.
	 * <p>
	 * Query should be in the following form:
	 *
	 * <pre>key.value_key2.value2.value3</pre>
	 * (`_` separating key/value pairs, `.` separating keys and values)
	 * <p>
	 * To describe the following keyword map:
	 *
	 * <pre>
	 *     {
	 *         "key": "value",
	 *         "key2": "value2" || "value3"
	 *     }
	 * </pre>
	 * <p>
	 * TODO:: test
	 *
	 * @param query
	 *
	 * @return
	 */
	public static Bson getAttSearchBson(String query) {
		if (query == null || query.isBlank()) {
			return null;
		}
		
		String[] keywordPairs = query.split("_");
		List<Bson> filters = new ArrayList<>(keywordPairs.length);
		
		for (String curKvPair : keywordPairs) {
			String[] keyValue = curKvPair.split("\\.");
			if (keyValue.length == 0) {
				throw new IllegalArgumentException("Bad keyword(s) string; extra \"_\"");
			}
			String key = keyValue[0];
			List<Bson> curKvFilters = new ArrayList<>(keyValue.length);
			
			filters.add(Filters.exists(keyValue[0]));
			
			for (int i = 1; i < keyValue.length; i++) {
				curKvFilters.add(
					eq(key, getSearchTermPattern(keyValue[i]))
				);
			}
			
			filters.add(
				or(curKvFilters)
			);
		}
		
		return and(filters);
	}
	
}
