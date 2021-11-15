package com.ebp.openQuarterMaster.lib.core.storage;

import com.ebp.openQuarterMaster.lib.core.MainObject;
import com.ebp.openQuarterMaster.lib.core.storage.stored.Stored;
import com.ebp.openQuarterMaster.lib.core.storage.stored.StoredType;
import com.ebp.openQuarterMaster.lib.core.validation.annotations.ValidInventoryItem;
import com.ebp.openQuarterMaster.lib.core.validation.annotations.ValidUnit;
import lombok.*;
import org.bson.types.ObjectId;
import tech.units.indriya.AbstractUnit;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    @NotBlank(message = "Name cannot be blank")
    private String name;

    /**
     * Keywords associated with this item. Used for searching for items.
     */
    @NonNull
    @NotNull
    private List<@NotBlank String> keywords = new ArrayList<>();

    /**
     * The type of storage this item uses.
     */
    @NonNull
    @NotNull
    private StoredType storedType;

    /**
     * The map of where the items are stored.
     */
    @NonNull
    @NotNull
    private Map<@NonNull ObjectId, List<Stored>> storageMap = new LinkedHashMap<>();

    /**
     * The unit used to measure the item.
     * Will always be {@link AbstractUnit#ONE} if {@link #storedType} is {@link StoredType#AMOUNT}
     */
    @NonNull
    @ValidUnit
    private Unit unit = AbstractUnit.ONE;

    /**
     * The total amount of that item in storage, in the {@link #unit} unit.
     */
    @Setter(AccessLevel.PRIVATE)
    private Quantity total = null;

    /**
     * The name of the identifier used for the items tracked.
     * <p>
     * Example might be serial number.
     *
     * Only for use when {@link #storedType} is {@link StoredType#TRACKED}
     */
    private String trackedItemIdentifierName = null;

    {
        this.recalcTotal();
    }

    private InventoryItem(String name, StoredType storedType) {
        this.setName(name);
        this.setStoredType(storedType);
    }

    public InventoryItem(String name, Unit unit) {
        this(name, StoredType.AMOUNT);
        this.setUnit(unit);
    }

    public InventoryItem(String name, String trackedItemIdentifierName) {
        this(name, StoredType.TRACKED);
        this.setUnit(AbstractUnit.ONE);
        this.setTrackedItemIdentifierName(trackedItemIdentifierName);
    }

    /**
     * Recalculates the total amount of the item stored.
     * <p>
     * Calculates the total, sets {@link #total}, and returns the value.
     *
     * @return The total amount stored.
     */
    public Quantity recalcTotal() {
        Quantity total = Quantities.getQuantity(0, this.getUnit());
        for (List<Stored> storedList : this.getStorageMap().values()) {
            for (Stored stored : storedList) {
                total = total.add(stored.getAmount());
            }
        }
        this.setTotal(total);
        return this.getTotal();
    }

    //TODO:: add stored
    //TODO:: remove stored
    //TODO:: transfer
}
