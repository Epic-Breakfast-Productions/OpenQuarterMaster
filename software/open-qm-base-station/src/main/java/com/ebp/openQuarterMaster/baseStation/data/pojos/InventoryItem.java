package com.ebp.openQuarterMaster.baseStation.data.pojos;

import com.ebp.openQuarterMaster.baseStation.data.pojos.exceptions.UnitNotSupportedException;
import lombok.Builder;
import lombok.Data;
import systems.uom.common.USCustomary;
import tech.units.indriya.AbstractUnit;
import tech.units.indriya.unit.Units;

import javax.measure.Quantity;
import javax.measure.Unit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Data
@Builder
public class InventoryItem<M extends Quantity<M>> {
    public static final List<Unit> ALLOWED_MEASUREMENTS = Collections.unmodifiableList(Arrays.asList(
            //amounts
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
    ));

    /**
     * The name of this inventory item
     */
    private String name;
    /**
     * Keywords associated with this item. Used for searching for items.
     */
    @Builder.Default
    private List<String> keywords = new ArrayList<>();
    /**
     * How we keep track of this item
     */
    @Builder.Default
    private TrackType trackType = TrackType.COUNT;
    /**
     * The unit used to measure the item
     */
    private Unit<M> unit;

    public InventoryItem<M> setUnit(Unit<M> unit) throws UnitNotSupportedException {
        if (!ALLOWED_MEASUREMENTS.contains(unit)) {
            throw new UnitNotSupportedException(unit);
        }
        this.unit = unit;
        return this;
    }
}
