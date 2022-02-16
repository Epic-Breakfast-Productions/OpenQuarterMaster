package com.ebp.openQuarterMaster.lib.core.storage.items;

import com.ebp.openQuarterMaster.lib.core.storage.items.stored.AmountStored;
import com.ebp.openQuarterMaster.lib.core.validation.annotations.ValidUnit;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import tech.units.indriya.AbstractUnit;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.validation.constraints.NotNull;
import java.util.List;


@EqualsAndHashCode(callSuper = true)
@Data
public class AmountItem extends InventoryItem<List<@NotNull AmountStored>> {

    /**
     * The unit used to measure the item.
     */
    @NonNull
    @ValidUnit
    private Unit<?> unit = AbstractUnit.ONE;

    @Override
    public Quantity<?> recalcTotal() {
        //TODO:: try parallel streams
        Quantity<?> total = Quantities.getQuantity(0, this.getUnit());
        for (List<AmountStored> storedList : this.getStorageMap().values()) {
            for (AmountStored amtStored : storedList) {
                Quantity amount = amtStored.getAmount();
                if (amount == null) {
                    continue;
                }
                total = total.add(amount);
            }
        }
        this.setTotal(total);
        return this.getTotal();
    }

    @Override
    public long numStored() {
        return this.getStorageMap()
                .values()
                .parallelStream()
                .mapToLong(List::size)
                .sum();
    }
}
