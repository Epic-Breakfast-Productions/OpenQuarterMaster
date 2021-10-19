package com.ebp.openQuarterMaster.lib.core.storage.stored;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 * Describes a unique item stored.
 */
@Data
@NoArgsConstructor
public class TrackedItem {
    /** Internal id of the thing stored. */
    private UUID id;
    /** Attributes related to the item */
    private Map<String, String> attributes;
}
