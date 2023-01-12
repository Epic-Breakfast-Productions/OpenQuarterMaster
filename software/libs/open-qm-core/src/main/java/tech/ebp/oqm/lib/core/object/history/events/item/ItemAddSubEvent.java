package tech.ebp.oqm.lib.core.object.history.events.item;

import lombok.NoArgsConstructor;
import tech.ebp.oqm.lib.core.object.history.DescriptiveEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.measure.Quantity;


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
}
