package tech.ebp.oqm.baseStation.model.rest.externalService;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.model.rest.auth.externalService.ExternalServiceLoginRequest;

import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ExternalServiceSetupResponse {
	
	@NonNull
	@NotNull
	private ObjectId id;
	
	@NonNull
	@NotNull
	private String setupToken;
	
	@NotNull
	@NonNull
	@lombok.Builder.Default
	private List<String> grantedRoles = new ArrayList<>();
	
	public ExternalServiceLoginRequest toLoginRequest() {
		return ExternalServiceLoginRequest.builder()
										  .setupToken(this.getSetupToken())
										  .id(this.getId())
										  .build();
	}
}
