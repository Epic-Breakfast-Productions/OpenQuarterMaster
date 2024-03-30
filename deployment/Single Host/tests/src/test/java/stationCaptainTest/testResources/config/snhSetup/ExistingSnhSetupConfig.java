package stationCaptainTest.testResources.config.snhSetup;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Setup to specify an existing host to test against
 *
 * Connects via ssh:
 * https://www.baeldung.com/java-ssh-connection
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
public class ExistingSnhSetupConfig extends SnhSetupConfig {
	
	private String host;
	private int port = 22;
	private String user;
	private String password = null;
	
	
	@Override
	public SnhType getType() {
		return SnhType.EXISTING;
	}
}
