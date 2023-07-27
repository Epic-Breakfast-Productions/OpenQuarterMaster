package tech.ebp.oqm.baseStation.model.object.storage.items.stored;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import tech.ebp.oqm.baseStation.model.object.storage.items.TrackedItem;
import tech.ebp.oqm.baseStation.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.baseStation.model.object.storage.items.stored.StoredType;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

/**
 * Stored object to describe an individual, tracked item.
 * <p>
 * The key of this object in the {@link TrackedItem} object is the identifying key for this
 * object.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TrackedStored extends Stored {
	
	@NonNull
	//	@NotNull
	@NotBlank
	private String identifier;
	
	public TrackedStored(String identifier) {
		this();
		this.identifier = identifier;
	}
	
	/**
	 * Some extra details to help identify this exact item.
	 */
	private String identifyingDetails = "";
	
	/**
	 * The value of this particular tracked item.
	 * <p>
	 * If not set (null), will default to the default value held in the item during calculations.
	 */
	@DecimalMin("0.0")
	private BigDecimal value = null;
	
	@Override
	public tech.ebp.oqm.baseStation.model.object.storage.items.stored.StoredType getStoredType() {
		return StoredType.TRACKED;
	}
}
