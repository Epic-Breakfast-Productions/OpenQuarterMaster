package tech.ebp.oqm.core.api.model.rest.management;

import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@SuperBuilder
public class HistoriedCollectionClearResult extends CollectionClearResult {
	@NonNull
	private CollectionClearResult historyCollectionResult;
}
