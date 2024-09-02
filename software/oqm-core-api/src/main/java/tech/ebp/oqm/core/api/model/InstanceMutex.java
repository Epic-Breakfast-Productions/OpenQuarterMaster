package tech.ebp.oqm.core.api.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.core.api.model.object.MainObject;

import java.time.ZonedDateTime;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class InstanceMutex extends MainObject {
	@Override
	public int getSchemaVersion() {
		return 0;
	}

	public InstanceMutex(String mutexId){
		this.mutexId = mutexId;
	}

	private String mutexId;

	@lombok.Builder.Default
	private boolean taken = false;

	@lombok.Builder.Default
	private ZonedDateTime takenAt = null;

	@lombok.Builder.Default
	protected String takenBy = null;
}
