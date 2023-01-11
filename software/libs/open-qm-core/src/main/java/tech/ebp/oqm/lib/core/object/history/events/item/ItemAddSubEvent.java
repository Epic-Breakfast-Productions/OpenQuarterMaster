package tech.ebp.oqm.lib.core.object.history.events.item;

import lombok.NoArgsConstructor;
import tech.ebp.oqm.lib.core.object.history.DescriptiveEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import javax.measure.Quantity;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
//@SuperBuilder
public abstract class ItemAddSubEvent
	extends DescriptiveEvent
	//	implements AttKeywordContaining
{
	private Quantity<?> quantity;
	
	/**
	 * Attributes this object might have, usable for any purpose.
	 */
	@NotNull
	@NonNull
	@lombok.Builder.Default
	private Map<@NotBlank @NotNull String, String> attributes = new HashMap<>();
	
	/**
	 * Keywords for the object
	 */
	@NotNull
	@NonNull
	@lombok.Builder.Default
	private List<@NotBlank String> keywords = new ArrayList<>();
}
