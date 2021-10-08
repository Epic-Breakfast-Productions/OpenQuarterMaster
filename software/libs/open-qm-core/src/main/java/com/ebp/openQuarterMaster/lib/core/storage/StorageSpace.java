package com.ebp.openQuarterMaster.lib.core.storage;


import com.ebp.openQuarterMaster.lib.core.MainObject;
import com.ebp.openQuarterMaster.lib.core.history.Historied;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StorageSpace extends MainObject {
    private String name;
    private List<UUID> blocks;
}
