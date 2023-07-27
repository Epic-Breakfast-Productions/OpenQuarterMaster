package tech.ebp.oqm.baseStation.model.rest.auth.externalService;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * The request object for logging in a user
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ExternalServiceLoginRequest {
	
	@NonNull
	@NotNull
	private ObjectId id;
	
	@NonNull
	@NotNull
	@NotBlank
	private String setupToken;
}
