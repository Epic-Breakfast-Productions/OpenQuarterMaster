package com.ebp.openQuarterMaster.baseStation.service.mongo.search;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.*;

public class SearchUtils {
    private static final String ANY_NONE_OR_MANY_CHARS = "[\\s\\S]*";

    public static Pattern getSearchTermPattern(String term) {
        return Pattern.compile(
                ANY_NONE_OR_MANY_CHARS + term + ANY_NONE_OR_MANY_CHARS,
                Pattern.CASE_INSENSITIVE
        );
    }

    /**
     * TODO:: test
     *
     * @param field
     * @param sortType
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

    /**
     * Parses keyword searches.
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
     *
     * TODO:: test
     * @param query
     * @return
     */
    public static Bson getAttSearchBson(String query) {
        if(query == null || query.isBlank()){
            return null;
        }

        String[] keywordPairs = query.split("_");
        List<Bson> filters = new ArrayList<>(keywordPairs.length);

        for(String curKvPair : keywordPairs){
            String[] keyValue = curKvPair.split("\\.");
            if(keyValue.length == 0){
                throw new IllegalArgumentException("Bad keyword(s) string; extra \"_\"");
            }
            String key = keyValue[0];
            List<Bson> curKvFilters = new ArrayList<>(keyValue.length);

            filters.add(Filters.exists(keyValue[0]));

            for(int i = 1; i < keyValue.length; i++){
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
