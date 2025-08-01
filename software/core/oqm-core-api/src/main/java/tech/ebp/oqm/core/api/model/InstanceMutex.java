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
	public static final int CUR_SCHEMA_VERSION = 1;
	
	@Override
	public int getSchemaVersion() {
		return CUR_SCHEMA_VERSION;
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
