package tech.ebp.oqm.core.api.model.object.interactingEntity.externalService;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntityType;

@Data
@NoArgsConstructor
//@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@Schema(title = "GeneralService", description = "A non-user service.")
public class GeneralService extends ExternalService {
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@Override
	@Schema(constValue = "SERVICE_GENERAL", readOnly = true, required = true, examples = "SERVICE_GENERAL")
	public InteractingEntityType getType() {
		return InteractingEntityType.SERVICE_GENERAL;
	}
	
	@Override
	public boolean updateFrom(JsonWebToken jwt) {
		//TODO
		return false;
	}
}
