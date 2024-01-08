package stationCaptainTest.testResources.snhConnector;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.channel.Channel;
import org.apache.sshd.scp.client.ScpClient;
import org.apache.sshd.scp.client.ScpClientCreator;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;
import stationCaptainTest.testResources.config.snhSetup.ExistingSnhSetupConfig;
import stationCaptainTest.testResources.config.snhSetup.SnhType;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
public class ExistingSnhConnector extends SnhConnector<ExistingSnhSetupConfig> {
	private static final int DEFAULT_TIMEOUT = 60;
	private static final TimeUnit DEFAULT_TIMEOUT_UNIT = TimeUnit.SECONDS;
	
	private SshClient client = SshClient.setUpDefaultClient();
	private ClientSession clientSession;
	private ScpClient scpClient;
	
	public ExistingSnhConnector(){
		try {
			this.client.start();
			log.info("Connecting to remote SSH server.");
			this.clientSession = this.client.connect(
				this.getSetupConfig().getUser(),
				this.getSetupConfig().getHost(),
				this.getSetupConfig().getPort()
			).verify(DEFAULT_TIMEOUT, DEFAULT_TIMEOUT_UNIT).getSession();
			log.info("Connected to remote SSH server.");
			if(this.getSetupConfig().getPassword() != null) {
				log.info("Password given in config. Setting up password.");
				this.clientSession.addPasswordIdentity(this.getSetupConfig().getPassword());
				this.clientSession.auth().verify(DEFAULT_TIMEOUT, DEFAULT_TIMEOUT_UNIT);
				log.info("Password setup.");
			}
			
			ScpClientCreator scpClientCreator = ScpClientCreator.instance();
			this.scpClient = scpClientCreator.createScpClient(this.getClientSession());
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		log.info("SSH connection setup.");
	}
	
	@Override
	public SnhType getType() {
		return SnhType.EXISTING;
	}
	
	@Override
	public CommandResult runCommand(String... command) {
		String commandSent =
			"echo \""+this.getSetupConfig().getPassword()+"\" | sudo -S " +
			Arrays.stream(command)
				.map((commandPart)->commandPart.replaceAll(" ", "\\ "))
				.collect(Collectors.joining (" "));
		log.info("Sending command to external SNH: {}", commandSent);
		try (
			ByteArrayOutputStream stdOutStream = new ByteArrayOutputStream();
			ByteArrayOutputStream stdErrStream = new ByteArrayOutputStream();
			ClientChannel channel = this.getClientSession().createExecChannel(commandSent)
		) {
			channel.setOut(stdOutStream);
			channel.setErr(stdErrStream);
			try {
				channel.open().verify(DEFAULT_TIMEOUT, DEFAULT_TIMEOUT_UNIT);
				log.info("Command sent to external host. Waiting for reply.");
				channel.waitFor(
					EnumSet.of(ClientChannelEvent.CLOSED),
					0L //No timeout
				);
				log.info(
					"Got result from command. Exit status: {} / Exit signal: {} / stdOut: {} / stdErr: {}",
					channel.getExitStatus(),
					channel.getExitSignal(),
					stdOutStream.toString(),
					stdErrStream.toString()
				);
				
			} finally {
				channel.close(false);
			}
			return CommandResult.builder()
					   .stdOut(stdOutStream.toString())
					   .stdErr(stdErrStream.toString())
					   .returnCode(channel.getExitStatus())
					   .build();
		} catch (Exception e){
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void copyToHost(String destination, InputStream localFile) {
		try {
			Path temp = Files.createTempFile("oqm-test-file", "");
			try(OutputStream toTemp = Files.newOutputStream(temp)){
				log.debug("Writing data to temp file.");
				IOUtils.copy(localFile, toTemp);
			}
			
			log.debug("Uploading temp file to host: {} -> remote:{}", temp, destination);
			this.getScpClient().upload(temp, destination);
			Files.delete(temp);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void copyFromHost(String remoteFile, OutputStream destination) {
		try {
			this.getScpClient().download(remoteFile, destination);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		if(this.clientSession != null){
			this.getClientSession().close();
		}
	}
}
