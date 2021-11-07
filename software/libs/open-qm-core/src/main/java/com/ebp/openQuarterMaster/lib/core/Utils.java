package com.ebp.openQuarterMaster.lib.core;

import com.ebp.openQuarterMaster.lib.core.jackson.MongoObjectIdModule;
import com.ebp.openQuarterMaster.lib.core.jackson.TempQuantityJacksonModule;
import com.ebp.openQuarterMaster.lib.core.jackson.UnitModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import tech.uom.lib.jackson.UnitJacksonModule;

import java.util.TimeZone;

public class Utils {
    /**
     * A global object mapper that is pre-configured to handle all objects in lib.
     * <p>
     * Configured by {@link #setupObjectMapper(ObjectMapper)}.
     */
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    /**
     * Modules needed for proper serialization of lib objects. Registered with {@link #OBJECT_MAPPER}
     */
    public static final Module[] MAPPER_MODULES = {
            new UnitJacksonModule(),
            new JavaTimeModule(),
            new MongoObjectIdModule(),
            new TempQuantityJacksonModule(),
            new UnitModule()
    };

    static {
        setupObjectMapper(OBJECT_MAPPER);
    }

    /**
     * Configures a given object mapper with the proper configuration to handle all lib objects.
     *
     * @param mapper The object mapper to set up.
     */
    public static void setupObjectMapper(ObjectMapper mapper) {
        mapper.registerModules(MAPPER_MODULES);

        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//        mapper.enable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID);
//        mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);

        //set the timezone to this server's.
        mapper.setTimeZone(TimeZone.getDefault());
    }

}
