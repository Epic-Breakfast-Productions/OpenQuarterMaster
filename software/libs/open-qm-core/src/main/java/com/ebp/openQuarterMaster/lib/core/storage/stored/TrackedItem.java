package com.ebp.openQuarterMaster.lib.core.storage.stored;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Describes a unique item stored.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackedItem {
    /** Internal id of the thing stored. */
    private UUID id;
    /** Attributes related to the item */
    private Map<String, String> attributes = new HashMap<>();

    public TrackedItem(UUID id){
        this.id = id;
    }
}
