package tech.ebp.oqm.core.api.model.object.storage.items.stored;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import tech.ebp.oqm.core.api.model.validation.annotations.ValidQuantity;

import javax.measure.Quantity;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@Schema(title = "UniqueWithAmountStored", description = "Stored object to describe a single unique item that also has an amount of stuff associated.")
public class UniqueWithAmountStored extends UniqueStored implements AmountContaining {

	@Schema(defaultValue = "UNIQUE")
	@Override
	public StoredType getType() {
		return StoredType.UNIQUE_WITH_AMOUNT;
	}

	/**
	 * The amount of the thing stored.
	 */
	@NotNull
	@NonNull
	@ValidQuantity
	@Schema(description = "The amount of something that is tracked in this object. Unit must be compatible with the associated InventoryItem entry. Cannot be changed after creation.")
	private Quantity<?> amount;

	@Override
	public String getDefaultLabelFormat() {
		return super.getDefaultLabelFormat() + " / {amt}";
	}
}
