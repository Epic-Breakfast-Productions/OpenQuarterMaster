package com.ebp.openQuarterMaster.lib.core.storage.stored;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.*;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class TrackedStored extends Stored {
    private Map<String, TrackedItem> items = new HashMap<>();
}
