package tech.ebp.oqm.core.api.model.object.upgrade;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.Duration;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Setter(AccessLevel.PRIVATE)
@SuperBuilder(toBuilder = true)
public class HistoriedCollectionUpgradeResult extends CollectionUpgradeResult {
	
	private CollectionUpgradeResult historyCollectionUpgradeResult;
}
