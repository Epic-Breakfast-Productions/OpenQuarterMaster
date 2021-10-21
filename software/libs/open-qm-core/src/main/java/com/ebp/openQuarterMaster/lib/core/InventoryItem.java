package com.ebp.openQuarterMaster.lib.core;

import com.ebp.openQuarterMaster.lib.core.storage.stored.Stored;
import com.ebp.openQuarterMaster.lib.core.storage.stored.StoredType;
import com.ebp.openQuarterMaster.lib.core.validation.annotations.ValidInventoryItem;
import com.ebp.openQuarterMaster.lib.core.validation.annotations.ValidUnit;
import lombok.*;
import tech.units.indriya.AbstractUnit;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * Describes a type of inventory item.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ValidInventoryItem
public class InventoryItem extends MainObject {
    /**
     * The name of this inventory item
     */
    @NonNull
    @NotNull
    @NotBlank(message = "Name cannot be blank")
    private String name;

    /**
     * Keywords associated with this item. Used for searching for items.
     */
    @NonNull
    @NotNull
    private List<@NotBlank String> keywords = new ArrayList<>();

    @NonNull
    @NotNull
    private StoredType storedType;

    @NonNull
    @NotNull
    private Map<UUID, Stored> storageMap = new HashMap<>();

    /**
     * The unit used to measure the item.
     * Will always be {@link AbstractUnit#ONE} if {@link #storedType} is {@link StoredType#AMOUNT}
     */
    @NonNull
    @NotNull
    @ValidUnit
    private Unit unit = AbstractUnit.ONE;

    /**
     * The total amount of that item in storage, in the {@link #unit} unit.
     */
    private Quantity total = null;

    /**
     * The name of the identifier used for the items tracked.
     *
     * Example might be serial number
     */
    private String trackedItemIdentifierName = null;

    /**
     * Recalculates the total amount of the item stored.
     *
     * Calculates the total, sets {@link #total}, and returns the value.
     *
     * @return The total amount stored.
     */
    public Quantity recalcTotal(){
//        AmountStored total = new AmountStored(this.unit);
//
//        for(AmountStored cur : this.getStorageMap().values()){
//            total = total.add(cur);
//        }
//
//        this.setTotal(total);
        return this.getTotal();
    }

}
