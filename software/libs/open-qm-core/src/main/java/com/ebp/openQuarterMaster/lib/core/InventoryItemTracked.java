package com.ebp.openQuarterMaster.lib.core;

import com.ebp.openQuarterMaster.lib.core.storage.stored.TrackedStored;
import lombok.*;
import tech.units.indriya.AbstractUnit;

/**
 * An inventory item that is kept track of by individual, unique pieces. Think Serial numbers of devices.
 *
 * If you are trying to track individual numbers of non-unique items, use {@link InventoryItemAmt} with {@link AbstractUnit#ONE} as the unit.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class InventoryItemTracked extends InventoryItem<TrackedStored> {
    /**
     * The name of the identifier used for the items tracked.
     *
     * Example might be serial number
     */
    private String trackedItemIdentifierName = null;
}
