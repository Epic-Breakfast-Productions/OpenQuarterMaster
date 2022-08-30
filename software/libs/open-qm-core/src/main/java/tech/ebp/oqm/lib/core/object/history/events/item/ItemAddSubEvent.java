package tech.ebp.oqm.lib.core.object.history.events.item;

import tech.ebp.oqm.lib.core.object.history.events.DescriptiveEvent;
import tech.ebp.oqm.lib.core.object.history.events.EventType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import javax.measure.Quantity;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@ToString(callSuper = true)
public abstract class ItemAddSubEvent
	extends DescriptiveEvent
	//	implements AttKeywordContaining
{
	
	public ItemAddSubEvent(EventType type) {
		super(type);
	}
	
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
