package tech.ebp.oqm.core.api.model.rest.auth.externalService;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;

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
