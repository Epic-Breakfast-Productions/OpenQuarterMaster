package tech.ebp.oqm.core.api.model;

import lombok.*;
import tech.ebp.oqm.core.api.model.object.MainObject;

import java.time.ZonedDateTime;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstanceMutex extends MainObject {
	@Override
	public int getSchemaVersion() {
		return 0;
	}

	public InstanceMutex(String mutexId){
		this.mutexId = mutexId;
	}

	private String mutexId;

	private boolean taken = false;

	private ZonedDateTime takenAt = null;

	protected String takenBy = null;
}
