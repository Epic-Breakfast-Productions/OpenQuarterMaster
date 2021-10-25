package com.ebp.openQuarterMaster.lib.core.storage.stored;

import com.ebp.openQuarterMaster.lib.core.validation.annotations.ValidStored;
import lombok.*;
import tech.units.indriya.AbstractUnit;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Describes an item stored in the system.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ValidStored
@Setter(AccessLevel.PRIVATE)
public class Stored {
    /**
     * The type this object represents the storage of
     */
    @NonNull
    @NotNull
    private StoredType type;

    /**
     * The amount of the thing stored.
     * To only be used when {@link #type} is {@link StoredType#AMOUNT}
     */
    private Quantity amount = null;

    /**
     * Tracked items.
     * Key is the identifying string data, value is the tracked item.
     * To only be used when {@link #type} is {@link StoredType#TRACKED}
     */
    private Map<@NotBlank String, @NotNull TrackedItem> items = null;

    private Stored(StoredType type){
        this.setType(type);
    }
    public Stored(Quantity amount){
        this(StoredType.AMOUNT);
        if(amount == null){
            throw new NullPointerException("Amount cannot be null when using amount type.");
        }
        this.amount = amount;
    }
    public Stored(Number amount, Unit unit) {
        this(Quantities.getQuantity(amount, unit));
    }
    public Stored(Unit unit) {
        this(0, unit);
    }

    public Stored(Map<String, TrackedItem> items){
        this(StoredType.TRACKED);
        this.setItems(items);
    }

    //TODO:: test
    public Quantity getAmount(){
        switch (this.getType()){
            case AMOUNT:
                return this.amount;
            case TRACKED:
                return Quantities.getQuantity(this.getItems().size(), AbstractUnit.ONE);
            default:
                throw new IllegalStateException("Invalid type set. should not happen.");
        }
    }

    public Stored add(Quantity amount) {
        if(!StoredType.AMOUNT.equals(this.getType())){
            throw new IllegalStateException("Cannot add amount to non-amount type.");
        }
        this.setAmount(this.getAmount().add(amount));
        return this;
    }
    public Stored add(Map<String, TrackedItem> items) {
        if(!StoredType.TRACKED.equals(this.getType())){
            throw new IllegalStateException("Cannot add items to non-tracked type.");
        }
        this.getItems().putAll(items);
        return this;
    }

    public Stored add(String key, TrackedItem item) {
        if(!StoredType.TRACKED.equals(this.getType())){
            throw new IllegalStateException("Cannot add item to non-tracked type.");
        }
        this.getItems().put(key, item);
        return this;
    }

    public Stored add(Stored other) {
        if(!this.getType().equals(other.getType())){
            throw new IllegalStateException("Cannot add a stored with one type and another with a different type.");
        }
        switch (this.getType()){
            case AMOUNT:
                this.add(other.getAmount());
                break;
            case TRACKED:
                this.add(other.getItems());
                break;
            default:
                throw new IllegalStateException("Unsupported type held.");
        }
        return this;
    }


    //TODO:: get item at key
    //TODO:: removes
}
