package com.ebp.openQuarterMaster.lib.core.storage.stored;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
public class TrackedItem {
    private UUID id;
    private Map<String, String> attributes;
}
