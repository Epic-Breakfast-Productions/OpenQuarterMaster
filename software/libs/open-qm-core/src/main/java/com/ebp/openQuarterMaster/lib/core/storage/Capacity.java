package com.ebp.openQuarterMaster.lib.core.storage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.measure.Quantity;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Capacity<T extends Quantity<T>> {
    private Quantity<T> capacityMeasure = null;
    private String label = "";
}
