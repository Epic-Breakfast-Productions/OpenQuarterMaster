package com.ebp.openQuarterMaster.lib.core.storage.items;

import com.ebp.openQuarterMaster.lib.core.storage.items.stored.StoredType;
import com.ebp.openQuarterMaster.lib.core.storage.items.stored.TrackedStored;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import tech.units.indriya.AbstractUnit;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Map;

/**
 * Describes a unique item stored.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class TrackedItem extends InventoryItem<Map<@NotBlank String, @NotNull TrackedStored>> {

    /**
     * The name of the identifier used for the items tracked.
     * <p>
     * Example might be serial number.
     */
    @NonNull
    @NotNull
    @Size(max = 50)
    @NotBlank
    private String trackedItemIdentifierName;

    public TrackedItem() {
        super(StoredType.TRACKED);
    }

    @Override
    public @NonNull Unit<?> getUnit() {
        return AbstractUnit.ONE;
    }

    @Override
    public Quantity<?> recalcTotal() {
        this.setTotal(
                Quantities.getQuantity(
                        this.getStorageMap()
                                .values()
                                .parallelStream()
                                .mapToLong(Map::size)
                                .sum(),
                        this.getUnit()
                )
        );
        return this.getTotal();
    }

    @Override
    public long numStored() {
        return (long) this.getTotal().getValue();
    }
}
