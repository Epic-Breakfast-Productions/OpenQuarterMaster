package com.ebp.openQuarterMaster.baseStation.data.pojos;

import com.ebp.openQuarterMaster.baseStation.data.pojos.exceptions.UnitNotSupportedException;
import lombok.*;
import tech.units.indriya.AbstractUnit;

import javax.measure.Unit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItem extends Tracked {

    /**
     * The name of this inventory item
     */
    @NonNull
    private String name;

    /**
     * The id for this inventory item
     */
    @Builder.Default
    private UUID id = UUID.randomUUID();

    /**
     * Keywords associated with this item. Used for searching for items.
     */
    @Builder.Default
    @NonNull
    private List<String> keywords = new ArrayList<>();

    /**
     * The unit used to measure the item
     */
    @Builder.Default
    @NonNull
    private Unit unit = AbstractUnit.ONE;

    public InventoryItem setUnit(Unit unit) throws UnitNotSupportedException {
        if (unit == null) {
            throw new NullPointerException("Unit cannot be null");
        }
        if (!Utils.ALLOWED_MEASUREMENTS.contains(unit)) {
            throw new UnitNotSupportedException(unit);
        }
        this.unit = unit;
        return this;
    }
}
