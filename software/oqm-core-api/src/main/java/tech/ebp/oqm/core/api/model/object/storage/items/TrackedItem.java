package tech.ebp.oqm.core.api.model.object.storage.items;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.StorageType;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.TrackedStored;
import tech.ebp.oqm.core.api.model.object.storage.items.storedWrapper.trackedStored.TrackedMapStoredWrapper;
import tech.ebp.oqm.core.api.model.object.storage.items.utils.BigDecimalSumHelper;
import tech.ebp.oqm.core.api.model.units.OqmProvidedUnits;

import javax.measure.Unit;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Describes a unique item stored.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TrackedItem extends InventoryItem<TrackedStored, Map<String, TrackedStored>, TrackedMapStoredWrapper> {
	public static final int CUR_SCHEMA_VERSION = 1;
	
	@Override
	public StorageType getStorageType() {
		return StorageType.TRACKED;
	}
	
	/**
	 * The name of the identifier used for the items tracked.
	 * <p>
	 * Example might be serial number.
	 */
	@NonNull
	@NotNull
	@Size(max = 50)
	@NotBlank
	private String trackedItemIdentifierName;
	
	/**
	 * The default value of an item held, if unspecified.
	 */
	@NonNull
	@DecimalMin("0.0")
	private BigDecimal defaultValue = BigDecimal.ZERO;
	
	@BsonIgnore
	@JsonIgnore
	@Override
	public @NonNull Unit<?> getUnit() {
		return OqmProvidedUnits.UNIT;
	}
	
	@Override
	public BigDecimal recalcValueOfStored() {
		BigDecimalSumHelper helper = new BigDecimalSumHelper();
		
		for (TrackedMapStoredWrapper w : this.getStorageMap().values()) {
			helper.addAll(
				w.values()
				 .stream()
				 .map((TrackedStored s)->{
					 if (s.getValue() == null) {
						 return getDefaultValue();
					 }
					 return s.getValue();
				 })
			);
		}
		
		this.setValueOfStored(helper.getTotal());
		return this.getValueOfStored();
	}
	
	@Override
	protected TrackedMapStoredWrapper newWrapperInstance() {
		return new TrackedMapStoredWrapper();
	}

	@Override
	public int getSchemaVersion() {
		return CUR_SCHEMA_VERSION;
	}
}
