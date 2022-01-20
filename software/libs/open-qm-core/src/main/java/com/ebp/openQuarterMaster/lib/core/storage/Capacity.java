package com.ebp.openQuarterMaster.lib.core.storage;

import com.ebp.openQuarterMaster.lib.core.storage.storageBlock.StorageBlock;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.measure.Quantity;
import javax.validation.constraints.NotNull;

/**
 * Describes the capacity of a {@link StorageBlock}.
 * @param <T> The type of quantity used.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Capacity<T extends Quantity<T>> {
    /** The actual measure of the capacity */
    @NotNull
    private Quantity<T> capacityMeasure = null;
}
