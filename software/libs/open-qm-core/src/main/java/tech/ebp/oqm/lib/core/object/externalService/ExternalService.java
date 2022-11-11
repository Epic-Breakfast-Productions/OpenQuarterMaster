package tech.ebp.oqm.lib.core.object.externalService;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import tech.ebp.oqm.lib.core.object.AttKeywordMainObject;
import tech.ebp.oqm.lib.core.object.externalService.plugin.PluginService;
import tech.ebp.oqm.lib.core.object.externalService.roles.RequestedRole;
import tech.ebp.oqm.lib.core.validation.annotations.ValidServiceRole;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "serviceType"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = GeneralService.class, name = "GENERAL"),
	@JsonSubTypes.Type(value = PluginService.class, name = "PLUGIN"),
})
@BsonDiscriminator(key = "serviceType_mongo")
public abstract class ExternalService extends AttKeywordMainObject {
	
	@NonNull
	@NotNull
	@NotBlank
	@Size(max = 30)
	private String name;
	
	/**
	 * Only used when authmode == SELF
	 */
	@NonNull
	@NotNull
	private boolean disabled = true;
	
	/**
	 * Only used when authmode == SELF
	 */
	@NonNull
	@NotNull
	private Set<@Valid RequestedRole> requestedRoles = new HashSet<>();
	
	/**
	 * Only used when authmode == SELF
	 */
	@NonNull
	@NotNull
	private Set<@ValidServiceRole String> roles = new HashSet<>();
	
	/**
	 * Only used when authmode == SELF
	 */
	private String secretHash;
	
	/**
	 * Only used when authmode == SELF
	 */
	private String setupTokenHash;
	
	public abstract ServiceType getServiceType();
}
