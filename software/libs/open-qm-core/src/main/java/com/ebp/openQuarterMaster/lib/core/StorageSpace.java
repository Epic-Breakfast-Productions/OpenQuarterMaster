package com.ebp.openQuarterMaster.lib.core;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageSpace {
    private String name;
    private String identifier;
    private ZonedDateTime added;
    private boolean active;
}
