package tech.ebp.oqm.core.api.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntityType;

import java.util.HashSet;
import java.util.Set;

@Singleton
@NoArgsConstructor
@BsonDiscriminator
public class CoreApiInteractingEntity extends InteractingEntity {
	
	/**
	 * Don't change this. We ue this very specific ObjectId to identify the Base Station's specific entry in the db.
	 */
	public static final ObjectId BS_ID = new ObjectId("00000000AAAAAAAAAAFFFFFF");

	@Getter
	private String email = "";

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@Override
	public ObjectId getId() {
		return BS_ID;
	}

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@Override
	public String getName() {
		return "Core API";
	}

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@Override
	public InteractingEntityType getType() {
		return InteractingEntityType.CORE_API;
	}

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@Override
	public Set<String> getRoles() {
		return new HashSet<>(){{add("Yes");}};
	}

	@Override
	public boolean updateFrom(JsonWebToken jwt) {
		return false;
	}
}
