package tech.ebp.oqm.lib.core.rest.auth.service;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import tech.ebp.oqm.lib.core.validation.annotations.ValidServiceRole;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

/**
 * The request object for logging in a user
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "serviceType"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = GeneralLoginRequest.class, name = "GENERAL"),
	@JsonSubTypes.Type(value = PluginLoginRequest.class, name = "PLUGIN"),
})
@BsonDiscriminator(key = "serviceType_mongo")
public abstract class ServiceLoginRequest {
	
	@NonNull
	@NotNull
	@NotBlank
	private String serviceIdentifier;
	
	@NonNull
	@NotNull
	@NotBlank
	private String password;
	
	@NonNull
	@NotNull
	private Set<@ValidServiceRole String> requestedRoles = new HashSet<>();
}
