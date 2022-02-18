package com.ebp.openQuarterMaster.lib.core.storage.items;

import com.ebp.openQuarterMaster.lib.core.ImagedMainObject;
import com.ebp.openQuarterMaster.lib.core.storage.items.stored.StoredType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.types.ObjectId;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Describes a type of inventory item.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "storedType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AmountItem.class, name = "AMOUNT"),
        @JsonSubTypes.Type(value = TrackedItem.class, name = "TRACKED")
})
@BsonDiscriminator
public abstract class InventoryItem<T> extends ImagedMainObject {

    /**
     * The name of this inventory item
     */
    @NonNull
    @NotBlank(message = "Name cannot be blank")
    private String name;

    /**
     * The type of storage this item uses.
     */
    @NonNull
    @NotNull
    private final StoredType storedType;

    /*TODO:: this useful?
     * Used to determine how much of something is stored if {@link #unit} is {@link AbstractUnit#ONE}
     * <p>
     * Example, if we store gallon jugs of Milk, we could use this to specify each jug holds a gallon, and compute we have n gallons of milk
     */
//    private Quantity<?> unitQuantity = null;

    /**
     * Description of the item
     */
    private String description = null;

    /**
     * The map of where the items are stored.
     */
    @NonNull
    @NotNull
    private Map<@NonNull ObjectId, @NonNull T> storageMap = new LinkedHashMap<>();
    
    /**
     * The total amount of that item in storage, in the {@link #getUnit()} unit.
     */
    @Setter(AccessLevel.PROTECTED)
    private Quantity<?> total = null;
    
    
    /**
     * Constructor to make Lombok happy.
     * <p>
     * Disregard, not intended to be used
     */
    public InventoryItem() {
        //noinspection ConstantConditions
        this.storedType = null;
        throw new UnsupportedOperationException();
    }

    protected InventoryItem(StoredType type) {
        this.storedType = type;
    }
    
    public abstract @NonNull Unit<?> getUnit();
    
    /**
     * Recalculates the total amount of the item stored.
     * <p>
     * Calculates the total, sets {@link #total}, and returns the value.
     *
     * @return The total amount stored.
     */
    public abstract Quantity<?> recalcTotal();
    
    public Quantity<?> getTotal() {
        if (total == null) {
            this.recalcTotal();
        }
        return this.total;
    }
    
    /**
     * Gets the number of individual entities stored
     *
     * @return the number of individual entities stored
     */
    public abstract long numStored();
    
    //TODO:: add stored
    //TODO:: remove stored
    //TODO:: transfer
}
