package tech.ebp.oqm.lib.core.object.history.events.item;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import tech.ebp.oqm.lib.core.object.history.events.HistoryEvent;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public abstract class ItemStorageBlockEvent extends HistoryEvent {
	
	@NotNull
	@NonNull
	private ObjectId storageBlockId;
	
	@lombok.Builder.Default
	private String identifier = null;
	
	@lombok.Builder.Default
	private Integer index = null;
}
