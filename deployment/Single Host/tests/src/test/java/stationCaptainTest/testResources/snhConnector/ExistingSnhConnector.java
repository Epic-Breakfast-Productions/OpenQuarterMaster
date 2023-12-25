package stationCaptainTest.testResources.snhConnector;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.session.ClientSession;
import stationCaptainTest.testResources.config.snhSetup.ExistingSnhSetupConfig;
import stationCaptainTest.testResources.config.snhSetup.SnhType;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * https://www.baeldung.com/java-ssh-connection#1-implementation-1
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
public class ExistingSnhConnector extends SnhConnector<ExistingSnhSetupConfig> {
	
	private SshClient client = SshClient.setUpDefaultClient();
	private ClientSession clientSession;
	
	@Override
	public void init() {
		try {
			this.clientSession = this.client.connect(
				this.getSetupConfig().getUser(),
				this.getSetupConfig().getHost(),
				this.getSetupConfig().getPort()
				).verify(60, TimeUnit.SECONDS).getSession();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		//TODO:: this
//		this.clientSession.addPasswordIdentity(password);
//		session.auth().verify(defaultTimeoutSeconds, TimeUnit.SECONDS);
	}
	
	@Override
	public SnhType getType() {
		return SnhType.EXISTING;
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		if(this.clientSession != null){
			this.getClientSession().close();
		}
	}
}
