package com.ebp.openQuarterMaster.lib.core.storage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.measure.Quantity;

/**
 * Describes the capacity of a {@link StorageBlock}.
 * @param <T> The type of quantity used.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Capacity<T extends Quantity<T>> {
    /** The actual measure of the capacity */
    private Quantity<T> capacityMeasure = null;
}
