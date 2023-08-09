package tech.ebp.oqm.baseStation.model.object.storage.items.storedWrapper.amountStored;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import tech.ebp.oqm.baseStation.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.baseStation.model.object.storage.items.storedWrapper.ListStoredWrapper;
import tech.ebp.oqm.baseStation.model.object.storage.items.utils.QuantitySumHelper;
import tech.ebp.oqm.baseStation.model.validation.annotations.ValidUnit;

import javax.measure.Quantity;
import javax.measure.Unit;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO:: figure out validator to ensure unit validity
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ListAmountStoredWrapper
	extends ListStoredWrapper<AmountStored>
{
	
	@NonNull
	@NotNull
	@ValidUnit
	private Unit parentUnit;
	
	@NonNull
	@NotNull
	private List<@NotNull AmountStored> stored = new ArrayList<>();
	
	public ListAmountStoredWrapper(Unit<?> parentUnit) {
		this.setParentUnit(parentUnit);
	}
	
	@Override
	public Quantity<?> recalcTotal() {
		QuantitySumHelper helper = new QuantitySumHelper(this.getParentUnit());
		
		helper.addAll(
			this.stream()
				.map(AmountStored::getAmount)
		);
		
		this.setTotal(helper.getTotal());
		return this.getTotal();
	}
}
