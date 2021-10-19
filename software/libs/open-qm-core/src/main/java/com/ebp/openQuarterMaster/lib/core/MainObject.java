package com.ebp.openQuarterMaster.lib.core;

import com.ebp.openQuarterMaster.lib.core.history.Historied;
import lombok.*;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MainObject extends Historied {
    private ObjectId id;
    private Map<String, String> attributes = new HashMap<>();
}
