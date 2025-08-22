package tech.ebp.oqm.core.api.model.object.storage.items.stored.stats;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bson.types.ObjectId;

import javax.measure.Unit;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ItemStoredStats extends StatsWithTotalContaining {
	
	public ItemStoredStats(Unit<?> unit) {
		super(unit);
	}
	
	private Map<ObjectId, StoredInBlockStats> storageBlockStats = new LinkedHashMap<>();
	private boolean lowStock = false;
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public boolean isAnyLowStock() {
		return this.lowStock || this.getNumLowStock() != 0;
	}
}
