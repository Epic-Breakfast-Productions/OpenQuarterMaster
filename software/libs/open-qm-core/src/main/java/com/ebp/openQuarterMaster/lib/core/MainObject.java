package com.ebp.openQuarterMaster.lib.core;

import com.ebp.openQuarterMaster.lib.core.history.Historied;
import lombok.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MainObject extends Historied {
    private UUID id;
    private Map<String, String> attributes = new HashMap<>();
}
