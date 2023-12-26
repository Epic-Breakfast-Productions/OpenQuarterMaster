package stationCaptainTest.testResources.snhConnector;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.channel.Channel;
import stationCaptainTest.testResources.config.snhSetup.ExistingSnhSetupConfig;
import stationCaptainTest.testResources.config.snhSetup.SnhType;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * https://www.baeldung.com/java-ssh-connection#1-implementation-1
 */
@Slf4j
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ExistingSnhConnector extends SnhConnector<ExistingSnhSetupConfig> {
	private static final int DEFAULT_TIMEOUT = 60;
	private static final TimeUnit DEFAULT_TIMEOUT_UNIT = TimeUnit.SECONDS;
	
	private SshClient client = SshClient.setUpDefaultClient();
	private ClientSession clientSession;
	
	@Override
	public void init(boolean install) {
		try {
			this.clientSession = this.client.connect(
				this.getSetupConfig().getUser(),
				this.getSetupConfig().getHost(),
				this.getSetupConfig().getPort()
				).verify(DEFAULT_TIMEOUT, DEFAULT_TIMEOUT_UNIT).getSession();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		//TODO:: this
//		this.clientSession.addPasswordIdentity(password);
//		session.auth().verify(defaultTimeoutSeconds, TimeUnit.SECONDS);
		super.init(install);
	}
	
	@Override
	public SnhType getType() {
		return SnhType.EXISTING;
	}
	
	@Override
	public CommandResult runCommand(String... command) {
		try (
			ByteArrayOutputStream stdOutStream = new ByteArrayOutputStream();
			ByteArrayOutputStream stdErrStream = new ByteArrayOutputStream();
			ClientChannel channel = this.getClientSession().createChannel(Channel.CHANNEL_SHELL)
		) {
			channel.setOut(stdOutStream);
			channel.setErr(stdErrStream);
			try {
				channel.open().verify(DEFAULT_TIMEOUT, DEFAULT_TIMEOUT_UNIT);
				try (OutputStream pipedIn = channel.getInvertedIn()) {
					String commandSent = Arrays.stream(command).map((commandPart)->commandPart.replaceAll(" ", "\\ ")).collect(Collectors.joining (" "));
					log.info("Sending command to external SNH: {}", commandSent);
					pipedIn.write(commandSent.getBytes());
					pipedIn.flush();
				}
				
				channel.waitFor(
					EnumSet.of(ClientChannelEvent.CLOSED),
					TimeUnit.SECONDS.toMillis(DEFAULT_TIMEOUT)
				);
				
				return CommandResult.builder()
					.stdOut(stdOutStream.toString())
					.stdErr(stdErrStream.toString())
					.returnCode(channel.getExitStatus())
					.build();
			} finally {
				channel.close(false);
			}
		} catch (Exception e){
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void copyToHost(String destination, File localFile) {
		//TODO
	}
	
	@Override
	public void copyFromHost(String remoteFile, File destination) {
		//TODO
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		if(this.clientSession != null){
			this.getClientSession().close();
		}
	}
}
