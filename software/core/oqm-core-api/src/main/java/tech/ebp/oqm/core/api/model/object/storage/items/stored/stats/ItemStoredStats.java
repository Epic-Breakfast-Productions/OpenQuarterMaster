package tech.ebp.oqm.core.api.model.object.storage.items.stored.stats;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;

import javax.measure.Unit;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public class ItemStoredStats extends StatsWithTotalContaining {
	
	public ItemStoredStats(Unit<?> unit) {
		super(unit);
		this.storageBlockStats = new LinkedHashMap<>();
	}
	
	@NonNull
	@NotNull
	@lombok.Builder.Default
	private Map<ObjectId, StoredInBlockStats> storageBlockStats = new LinkedHashMap<>();
	
	@lombok.Builder.Default
	private boolean lowStock = false;
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public boolean isAnyLowStock() {
		return this.lowStock || this.getNumLowStock() != 0;
	}
}
