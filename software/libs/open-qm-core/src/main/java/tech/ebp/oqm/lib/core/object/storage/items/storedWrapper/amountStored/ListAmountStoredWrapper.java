package tech.ebp.oqm.lib.core.object.storage.items.storedWrapper.amountStored;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import tech.ebp.oqm.lib.core.object.storage.items.exception.NotEnoughStoredException;
import tech.ebp.oqm.lib.core.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.lib.core.object.storage.items.storedWrapper.ListStoredWrapper;
import tech.ebp.oqm.lib.core.object.storage.items.utils.QuantitySumHelper;
import tech.ebp.oqm.lib.core.validation.annotations.ValidUnit;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

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
