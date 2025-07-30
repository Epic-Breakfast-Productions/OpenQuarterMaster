package tech.ebp.oqm.core.api.model.object.storage.items.stored.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bson.types.ObjectId;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class StoredStats extends BasicStatsContaining {

	private Map<ObjectId, ItemStoredStats> itemStats = new LinkedHashMap<>();
}
