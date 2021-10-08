package com.ebp.openQuarterMaster.lib.core;

import com.ebp.openQuarterMaster.lib.core.storage.stored.AmountStored;
import com.ebp.openQuarterMaster.lib.core.storage.stored.Stored;
import com.ebp.openQuarterMaster.lib.core.validators.ValidUnit;
import lombok.*;
import tech.units.indriya.AbstractUnit;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import javax.measure.UnitConverter;
import javax.measure.Unit;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * An inventory item that is kept track of by the amount of that item or substance.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class InventoryItemAmt extends InventoryItem<AmountStored> {
    /**
     * The unit used to measure the item
     */
    @NonNull
    @NotNull
    @ValidUnit
    private Unit unit = AbstractUnit.ONE;

    /**
     * The total amount of that item in storage
     */
    private AmountStored total = new AmountStored(this.unit);

    /**
     * Recalculates the total amount of the item stored.
     *
     * Calculates the total, sets {@link #total}, and returns the value.
     *
     * @return The total amount stored.
     */
    public AmountStored recalcTotal(){
        AmountStored total = new AmountStored(this.unit);

        for(AmountStored cur : this.getStorageMap().values()){
            total = total.add(cur);
        }

        this.setTotal(total);
        return this.getTotal();
    }
}
