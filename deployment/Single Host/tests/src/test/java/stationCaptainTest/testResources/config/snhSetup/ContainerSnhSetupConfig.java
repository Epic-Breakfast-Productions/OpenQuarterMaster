package stationCaptainTest.testResources.config.snhSetup;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Setup config that specifies to use TestContainers to run a new container to test against.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
public class ContainerSnhSetupConfig extends SnhSetupConfig {
	
	private String imageName;
	private String imageTag;
	
	@Override
	public SnhType getType() {
		return SnhType.CONTAINER;
	}
}