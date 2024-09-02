package tech.ebp.oqm.core.api.model.object.interactingEntity.externalService.plugin;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.eclipse.microprofile.jwt.JsonWebToken;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntityType;
import tech.ebp.oqm.core.api.model.object.interactingEntity.externalService.ExternalService;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
public class PluginService extends ExternalService {
	public static final int CUR_SCHEMA_VERSION = 1;
	
	@NonNull
	@NotNull
	@lombok.Builder.Default
	List<Plugin> enabledPageComponents = new ArrayList<>();
	
	@NonNull
	@NotNull
	@lombok.Builder.Default
	List<Plugin> disabledPageComponents = new ArrayList<>();
	
	public List<Plugin> getAllPlugins() {
		List<Plugin> output = new ArrayList<>();
		output.addAll(this.getEnabledPageComponents());
		output.addAll(this.getDisabledPageComponents());
		
		return output;
	}
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@Override
	public InteractingEntityType getInteractingEntityType() {
		return InteractingEntityType.SERVICE_PLUGIN;
	}
	
	@Override
	public boolean updateFrom(JsonWebToken jwt) {
		//TODO
		return false;
	}

	@Override
	public int getSchemaVersion() {
		return CUR_SCHEMA_VERSION;
	}
}
