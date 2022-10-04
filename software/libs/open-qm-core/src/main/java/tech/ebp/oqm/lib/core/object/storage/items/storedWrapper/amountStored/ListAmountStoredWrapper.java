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
import tech.ebp.oqm.lib.core.validation.annotations.ValidUnit;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.validation.constraints.NotNull;

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
		var ref = new Object() {
			Quantity<?> total = Quantities.getQuantity(0, getParentUnit());
			
			public void addToTotal(Quantity quantity) {
				this.total = this.total.add(quantity);
			}
		};
		
		this.stream()
			.map(AmountStored::getAmount)
			.sequential()
			.forEach((Quantity q)->{
				ref.addToTotal(q);
			});
		
		this.setTotal(ref.total);
		return this.getTotal();
	}
}
