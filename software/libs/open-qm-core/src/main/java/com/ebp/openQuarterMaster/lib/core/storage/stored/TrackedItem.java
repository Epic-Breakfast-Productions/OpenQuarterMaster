package com.ebp.openQuarterMaster.lib.core.storage.stored;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bson.types.ObjectId;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Describes a unique item stored.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackedItem {
    /**
     * The condition of the stored object. 100 = mint, 0 = completely deteriorated. Null if N/A.
     */
    @Max(100)
    @Min(0)
    private Integer condition = null;

    /**
     * When the item expires. Null if it does not expire.
     */
    private ZonedDateTime expires = null;
    /**
     * List of images related to the object.
     */
    @NonNull
    @NotNull
    List<@NonNull ObjectId> imageIds = new ArrayList<>();
    /**
     * Attributes related to the item
     */
    private Map<String, String> attributes = new HashMap<>();
}
