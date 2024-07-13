package tech.ebp.oqm.core.api.config;

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

	public static final int CUR_SCHEMA_VERSION = 1;
	/**
	 * Don't change this. We ue this very specific ObjectId to identify the Base Station's specific entry in the db.
	 */
	public static final ObjectId BS_ID = new ObjectId("00000000AAAAAAAAAAFFFFFF");
	
	@Inject
	public CoreApiInteractingEntity(
		@ConfigProperty(name = "service.runBy.email", defaultValue = "")
		String email
	){
		this.email = email;
	}
	
	@Getter
	private String email;
	
	@Override
	public ObjectId getId() {
		return BS_ID;
	}
	
	@Override
	public String getName() {
		return "Core API";
	}
	
	@Override
	public InteractingEntityType getInteractingEntityType() {
		return InteractingEntityType.CORE_API;
	}
	
	@Override
	public Set<String> getRoles() {
		return new HashSet<>(){{add("Yes");}};
	}
	
	@Override
	public boolean updateFrom(JsonWebToken jwt) {
		return false;
	}

	@Override
	public int getSchemaVersion() {
		return CUR_SCHEMA_VERSION;
	}
}
