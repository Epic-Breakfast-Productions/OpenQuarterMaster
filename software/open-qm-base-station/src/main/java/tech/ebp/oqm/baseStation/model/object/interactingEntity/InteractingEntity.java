package tech.ebp.oqm.baseStation.model.object.interactingEntity;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import tech.ebp.oqm.baseStation.model.object.AttKeywordMainObject;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.externalService.GeneralService;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.externalService.plugin.PluginService;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.user.User;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;


@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "interactingEntityType"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = User.class, name = "USER"),
	@JsonSubTypes.Type(value = PluginService.class, name = "SERVICE_PLUGIN"),
	@JsonSubTypes.Type(value = GeneralService.class, name = "SERVICE_GENERAL"),
})
@BsonDiscriminator
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class InteractingEntity extends AttKeywordMainObject {
	
	@NonNull
	@NotNull
	@NotBlank
	private String idFromAuthProvider;
	@NonNull
	@NotNull
	@NotBlank
	private String authProvider;
	
	public abstract String getName();
	
	public abstract String getEmail();
	
	public abstract InteractingEntityType getInteractingEntityType();
	
	public abstract Set<String> getRoles();
}
