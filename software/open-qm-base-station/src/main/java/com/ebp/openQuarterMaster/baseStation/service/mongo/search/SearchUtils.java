package com.ebp.openQuarterMaster.baseStation.service.mongo.search;

import com.mongodb.client.model.Sorts;
import org.bson.conversions.Bson;

import java.util.regex.Pattern;

public class SearchUtils {
    private static final String ANY_NONE_OR_MANY_CHARS = "[\\s\\S]*";

    public static Pattern getSearchTermPattern(String term){
        return Pattern.compile(
                ANY_NONE_OR_MANY_CHARS + term + ANY_NONE_OR_MANY_CHARS,
                Pattern.CASE_INSENSITIVE
        );
    }

    /**
     *
     * TODO:: test
     * @param field
     * @param sortType
     * @return The sort bson filter. Null if field is null or blank. Ascending by default.
     */
    public static Bson getSortBson(String field, SortType sortType){
        if(field == null || field.isBlank()){
            return null;
        }

        switch (sortType){
            case DESCENDING:
                return Sorts.descending(field);
            case ASCENDING:
            default:
                return Sorts.ascending(field);
        }
    }

}
