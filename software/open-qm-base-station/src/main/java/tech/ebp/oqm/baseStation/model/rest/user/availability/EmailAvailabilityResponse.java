package tech.ebp.oqm.baseStation.model.rest.user.availability;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.baseStation.model.rest.user.availability.AvailabilityResponse;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public class EmailAvailabilityResponse extends AvailabilityResponse {
	
	private String email;
}
