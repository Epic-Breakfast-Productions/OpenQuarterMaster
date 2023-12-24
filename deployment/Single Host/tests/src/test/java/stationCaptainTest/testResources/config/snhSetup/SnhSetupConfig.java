package stationCaptainTest.testResources.config.snhSetup;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = ContainerSnhSetupConfig.class, name = "CONTAINER"),
	@JsonSubTypes.Type(value = ExistingSnhSetupConfig.class, name = "EXISTING"),
})
public abstract class SnhSetupConfig {
	
	private InstallType installType;
	
	public abstract SnhType getType();
}