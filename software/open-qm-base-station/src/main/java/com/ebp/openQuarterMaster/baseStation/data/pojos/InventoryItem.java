package com.ebp.openQuarterMaster.baseStation.data.pojos;

import com.ebp.openQuarterMaster.baseStation.data.pojos.history.Historied;
import com.ebp.openQuarterMaster.baseStation.data.pojos.validators.ValidUnit;
import lombok.*;
import tech.units.indriya.AbstractUnit;

import javax.measure.Unit;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InventoryItem extends Historied {
    /**
     * The name of this inventory item
     */
    @NonNull
    @NotNull
    @NotBlank(message = "Name cannot be blank")
    private String name;

    /**
     * The id for this inventory item
     */
    @Builder.Default
    @NonNull
    @NotNull
    private UUID id = UUID.randomUUID();

    /**
     * Keywords associated with this item. Used for searching for items.
     */
    @Builder.Default
    @NonNull
    @NotNull
    private List<@NotBlank String> keywords = new ArrayList<>();

    @Builder.Default
    @NonNull
    @NotNull
    private Map<String, Object> attributes = new HashMap<>();

    /**
     * The unit used to measure the item
     */
    @Builder.Default
    @NonNull
    @NotNull
    @ValidUnit
    private Unit unit = AbstractUnit.ONE;
}
