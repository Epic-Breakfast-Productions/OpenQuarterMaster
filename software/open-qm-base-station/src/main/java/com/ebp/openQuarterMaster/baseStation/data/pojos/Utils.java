package com.ebp.openQuarterMaster.baseStation.data.pojos;

import com.fasterxml.jackson.databind.ObjectMapper;
import systems.uom.common.USCustomary;
import tech.units.indriya.AbstractUnit;
import tech.units.indriya.unit.Units;
import tech.uom.lib.jackson.UnitJacksonModule;

import javax.measure.Unit;
import java.util.List;

public class Utils {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        setupObjectMapper(OBJECT_MAPPER);
    }

    public static void setupObjectMapper(ObjectMapper mapper){
        mapper.registerModule(new UnitJacksonModule());
    }

    //amounts
    public static final List<Unit> ALLOWED_MEASUREMENTS = List.of(
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
