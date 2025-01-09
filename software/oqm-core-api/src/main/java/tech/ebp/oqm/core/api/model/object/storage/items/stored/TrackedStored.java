package tech.ebp.oqm.core.api.model.object.storage.items.stored;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import tech.ebp.oqm.core.api.model.object.storage.items.TrackedItem;

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
	public StoredType getStoredType() {
		return StoredType.TRACKED;
	}
	
	@Override
	public String getLabelText() {
		StringBuilder sb = new StringBuilder(this.getIdentifier());
		
		//TODO:: add more attributes
		
		return sb.toString();
	}
}
