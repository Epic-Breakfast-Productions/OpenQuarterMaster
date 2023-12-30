package stationCaptainTest.testResources.config.snhSetup.installType;


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
public class RepoInstallTypeConfig extends InstallTypeConfig {
	
	private String repoBranch = "main";
	
	public InstallType getType(){
		return InstallType.REPO;
	}
}
