package com.ebp.openQuarterMaster.lib.core;

import lombok.*;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Describes a main object that has images associated with it
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImagedMainObject extends MainObject {

    /**
     * List of images related to the object.
     */
    @NonNull
    @NotNull
    List<@NonNull ObjectId> imageIds = new ArrayList<>();
}
