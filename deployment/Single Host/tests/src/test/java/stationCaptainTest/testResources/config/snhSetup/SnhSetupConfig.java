package stationCaptainTest.testResources.config.snhSetup;

import lombok.Data;

@Data
public abstract class SnhSetupConfig {
	
	private InstallType installType;
	
	public abstract SnhType getType();
}
