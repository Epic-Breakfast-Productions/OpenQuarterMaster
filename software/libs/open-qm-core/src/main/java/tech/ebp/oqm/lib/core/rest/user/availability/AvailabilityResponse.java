package tech.ebp.oqm.lib.core.rest.user.availability;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public abstract class AvailabilityResponse {
	
	private boolean available;
}
