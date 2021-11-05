package com.ebp.openQuarterMaster.lib.core;

import com.ebp.openQuarterMaster.lib.core.jackson.MongoObjectIdModule;
import com.ebp.openQuarterMaster.lib.core.jackson.TempQuantityJacksonModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import systems.uom.common.USCustomary;
import tech.units.indriya.AbstractUnit;
import tech.units.indriya.unit.Units;
import tech.uom.lib.jackson.UnitJacksonModule;

import javax.measure.Unit;
import java.util.List;
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
            new TempQuantityJacksonModule()
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

    /**
     * List of units that are applicable to storage.
     */
    public static final List<Unit> ALLOWED_UNITS = List.of(
            AbstractUnit.ONE,
            Units.MOLE,
            // length
            Units.METRE,
            USCustomary.INCH,
            USCustomary.FOOT,
            USCustomary.FOOT_SURVEY,
            USCustomary.YARD,
            USCustomary.MILE,
            USCustomary.NAUTICAL_MILE,

            // mass
            Units.GRAM,
            Units.KILOGRAM,
            USCustomary.OUNCE,
            USCustomary.POUND,
            USCustomary.TON,

            //area
            Units.SQUARE_METRE,
            USCustomary.SQUARE_FOOT,
            USCustomary.ARE,
            USCustomary.HECTARE,
            USCustomary.ACRE,

            // volume
            Units.LITRE,
            Units.CUBIC_METRE,
            USCustomary.LITER,
            USCustomary.CUBIC_INCH,
            USCustomary.CUBIC_FOOT,
            USCustomary.ACRE_FOOT,
            USCustomary.GALLON_DRY,
            USCustomary.GALLON_LIQUID,
            USCustomary.FLUID_OUNCE,
            USCustomary.GILL_LIQUID,
            USCustomary.MINIM,
            USCustomary.FLUID_DRAM,
            USCustomary.CUP,
            USCustomary.TEASPOON,
            USCustomary.TABLESPOON,
            USCustomary.BARREL,
            USCustomary.PINT,

            //energy
            Units.JOULE
    );
}
