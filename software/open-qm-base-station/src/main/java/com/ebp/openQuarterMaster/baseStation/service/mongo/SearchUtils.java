package com.ebp.openQuarterMaster.baseStation.service.mongo;

import java.util.regex.Pattern;

public class SearchUtils {
    private static final String ANY_NONE_OR_MANY_CHARS = "[\\s\\S]*";

    public static Pattern getSearchTermPattern(String term){
        return Pattern.compile(
                ANY_NONE_OR_MANY_CHARS + term + ANY_NONE_OR_MANY_CHARS,
                Pattern.CASE_INSENSITIVE
        );
    }

}
