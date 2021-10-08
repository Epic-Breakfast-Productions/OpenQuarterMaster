package com.ebp.openQuarterMaster.lib.core;

import com.ebp.openQuarterMaster.lib.core.storage.stored.Stored;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * Describes a type of inventory item.
 * @param <S> The type of storage the item uses
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class InventoryItem<S extends Stored> extends MainObject {
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
    private Map<UUID, S> storageMap = new HashMap<>();
}
