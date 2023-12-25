package stationCaptainTest.testResources.config.snhSetup.installType;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import stationCaptainTest.testResources.config.snhSetup.ContainerSnhSetupConfig;
import stationCaptainTest.testResources.config.snhSetup.ExistingSnhSetupConfig;
import stationCaptainTest.testResources.config.snhSetup.SnhSetupConfig;
import stationCaptainTest.testResources.config.snhSetup.SnhType;

/**
 * Setup config that specifies to use TestContainers to run a new container to test against.
 */
@Data
@NoArgsConstructor
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = FilesInstallTypeConfig.class, name = "FILES"),
	@JsonSubTypes.Type(value = RepoInstallTypeConfig.class, name = "REPO"),
})
public abstract class InstallTypeConfig {
	
	public abstract InstallType getType();
}
