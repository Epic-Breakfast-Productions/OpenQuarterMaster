package stationCaptainTest.testResources.config.snhSetup;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.testcontainers.utility.DockerImageName;

/**
 * Setup config that specifies to use TestContainers to run a new container to test against.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
public class ContainerSnhSetupConfig extends SnhSetupConfig {
	
	private String imageName = "ubuntu";
	private String imageTag = "jammy";
	
	@Override
	public SnhType getType() {
		return SnhType.CONTAINER;
	}
	
	@JsonIgnore
	public DockerImageName getDockerImageName(){
		return DockerImageName.parse(this.getImageName() + ":" + this.getImageTag());
	}
}
