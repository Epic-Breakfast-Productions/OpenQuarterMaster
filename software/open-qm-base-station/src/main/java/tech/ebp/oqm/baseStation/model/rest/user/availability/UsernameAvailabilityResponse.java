package tech.ebp.oqm.baseStation.model.rest.user.availability;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public class UsernameAvailabilityResponse extends AvailabilityResponse {
	
	private String username;
}
