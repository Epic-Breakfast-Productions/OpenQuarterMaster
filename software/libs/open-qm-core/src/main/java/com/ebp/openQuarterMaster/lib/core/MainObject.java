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
    /** The id of this object in the Mongodb. */
    private ObjectId id;
    /** Attributes this object might have, usable for any purpose. */
    private Map<String, String> attributes = new HashMap<>();
}
