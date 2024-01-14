package stationCaptainTest.testResources.snhConnector;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import stationCaptainTest.testResources.config.ConfigReader;
import stationCaptainTest.testResources.config.snhSetup.ContainerSnhSetupConfig;
import stationCaptainTest.testResources.config.snhSetup.ExistingSnhSetupConfig;
import stationCaptainTest.testResources.config.snhSetup.SnhSetupConfig;
import stationCaptainTest.testResources.config.snhSetup.SnhType;
import stationCaptainTest.testResources.config.snhSetup.installType.RepoInstallTypeConfig;

import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
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
public abstract class SnhConnector<C extends SnhSetupConfig> implements Closeable {
	
	protected C getSetupConfig() {
		try {
			//noinspection unchecked
			return (C) ConfigReader.getTestRunConfig().getSetupConfig();
		} catch(ClassCastException | IOException e) {
			log.error("FAILED to cast config as appropriate: ", e);
			throw new RuntimeException("FAILED to cast config as appropriate.", e);
		}
	}
	
	public void init(boolean install) {
		this.setupForInstall();
		if (install) {
			this.installOqm(true);
		}
	}
	
	public abstract SnhType getType();
	
	public abstract CommandResult runCommand(String... command);
	
	public abstract void copyToHost(String destination, InputStream input);
	
	public void copyToHost(String remoteFile, File source) {
		try(
			FileInputStream is = new FileInputStream(source);
		){
			this.copyToHost(remoteFile, is);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void copyToHost(String destinationDir, Collection<File> localFiles) {
		log.info("Copying files to host into {}: {}", destinationDir, (Object) localFiles);
		for (File curFile : localFiles) {
			this.copyToHost(destinationDir + curFile.getName(), curFile);
		}
	}
	
	public void copyToHost(String destinationDir, File... localFiles) {
		this.copyToHost(destinationDir, localFiles);
	}
	
	public abstract void copyFromHost(String remoteFile, OutputStream destination);
	
	public void copyFromHost(String remoteFile, File destination) {
		try(
			FileOutputStream os = new FileOutputStream(destination);
		){
			this.copyFromHost(remoteFile, os);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	public void setupForInstall() {
		switch (this.getSetupConfig().getInstallTypeConfig().getType()){
			case REPO -> {
				log.info("Setting up host for repo install.");
				RepoInstallTypeConfig config = (RepoInstallTypeConfig) this.getSetupConfig().getInstallTypeConfig();
				
				String setupUrl = "https://deployment.openquartermaster.com/repos/"+config.getRepoBranch()+"/"+config.getInstallerType().name()+"/setup-repo.sh";
				log.debug("Setup script url: {}", setupUrl);
				CommandResult result = this.runCommand("wget", "-q", "-O", "/tmp/repoSetup.sh", setupUrl);
				if(result.getReturnCode()!= 0){
					log.error("FAILED to run command to download repo setup script. Error: {} / std out: {}", result.getStdErr(), result.getStdOut());
					throw new RuntimeException("FAILED to run command to download repo setup script (returned "+result.getReturnCode()+"): " + result.getStdErr() + " / " + result.getStdOut());
				}
				result = this.runCommand("chmod", "+x", "/tmp/repoSetup.sh");
				result = this.runCommand("/tmp/repoSetup.sh", "--auto");
				if(result.getReturnCode()!= 0){
					log.error("FAILED to run command to setup repo on host. Error: {}", result.getStdErr());
					throw new RuntimeException("FAILED to run command to setup repo on host (returned "+result.getReturnCode()+"): " + result.getStdErr() + " / " + result.getStdOut());
				}
			}
			case FILES -> {
				//build packages, transfer to host
				try {
					log.info("Building installers.");
					CommandResult.from(new ProcessBuilder("../Station-Captain/makeInstallers.sh")).assertSuccess("Build Station Captain Installers");
					CommandResult.from(new ProcessBuilder("../Infrastructure/makeInstallers.sh")).assertSuccess("Build Infrastructure Installers");
					CommandResult.from(new ProcessBuilder("../../../software/oqm-depot/makeSnhInstallers.sh")).assertSuccess("Build Depot Installers");
					CommandResult.from(new ProcessBuilder("../../../software/open-qm-base-station/makeInstallers.sh")).assertSuccess("Build Base Station Installers");
					log.info("Done building installers.");
				} catch(IOException | InterruptedException e) {
					throw new RuntimeException(e);
				}
				List<File> installers = new ArrayList<>();
				installers.addAll(List.of(new File("../Station-Captain/bin/").listFiles((FileFilter) new WildcardFileFilter("oqm-*."+ this.getSetupConfig().getInstallTypeConfig().getInstallerType().name()))));
				installers.addAll(List.of(new File("../Infrastructure/build/").listFiles((FileFilter) new WildcardFileFilter("oqm-*."+ this.getSetupConfig().getInstallTypeConfig().getInstallerType().name()))));
				installers.addAll(List.of(new File("../../../software/open-qm-base-station/build/installers/").listFiles((FileFilter) new WildcardFileFilter("oqm-*."+ this.getSetupConfig().getInstallTypeConfig().getInstallerType().name()))));
				installers.addAll(List.of(new File("../../../software/oqm-depot/build/installers/").listFiles((FileFilter) new WildcardFileFilter("oqm-*."+ this.getSetupConfig().getInstallTypeConfig().getInstallerType().name()))));
				log.info("Installers to add to host: {}", installers);
				
				this.runCommand("mkdir", "-p", "/tmp/oqm-installers/").assertSuccess("List uploaded installers.");
				this.runCommand("rm", "-rf", "/tmp/oqm-installers/*").assertSuccess("Remove previously uploaded installers.");
				this.runCommand("chmod", "777", "/tmp/oqm-installers").assertSuccess("Adjust permissions of installer upload dir.");
				log.info("Prepared destination directory.");
				this.copyToHost("/tmp/oqm-installers/", installers);
				log.info("Copied all files to host.");
				
				CommandResult result = this.runCommand("ls", "/tmp/oqm-installers/").assertSuccess("List uploaded installers.");
				log.info("Installers on remote box: {}", result.getStdOut());
			}
		}
	}
	
	public CommandResult installOqm(boolean verify) {
		CommandResult output = null;
		switch (this.getSetupConfig().getInstallTypeConfig().getInstallerType()) {
			case deb -> {
				switch (this.getSetupConfig().getInstallTypeConfig().getType()) {
					case REPO -> {
						output = this.runCommand("apt-get", "install", "-y", "open+quarter+master-core-base+station");
					}
					case FILES -> {
						output = this.runCommand("apt-get", "install", "-y", "/tmp/oqm-installers/*.deb");
					}
				}
			}
			case rpm -> {
				//TODO
			}
		}
		log.debug("OQM install return code: {}", output.getReturnCode());
		log.debug("OQM install stdout: {}", output.getStdOut());
		log.debug("OQM install stderr: {}", output.getStdErr());
		if(verify && output.getReturnCode() != 0){
			throw new RuntimeException("FAILED to install OQM: " + output.getStdErr());
		}
		return output;
	}
	
	public void uninstallOqm(){
		log.info("Uninstalling OQM");
		switch (this.getSetupConfig().getInstallTypeConfig().getInstallerType()){
			case deb -> {
				this.runCommand("apt-get", "remove", "-y", "--purge", "open+quarter+master-*");
				this.runCommand("apt-get", "remove", "-y", "--purge", "oqm-*");
				this.runCommand("docker", "image", "prune");
				this.runCommand("apt-get", "-y", "autoremove");
			}
			case rpm -> {
				//TODO
			}
		}
		this .runCommand("rm", "-rf", "/etc/oqm", "/tmp/oqm", "/data/oqm");
	}
	
	
	@Override
	public void close() throws IOException {
	}
	
	public static SnhConnector<?> fromConfig() throws IOException {
		switch (ConfigReader.getTestRunConfig().getSetupConfig().getType()){
			case EXISTING -> {
				return new ExistingSnhConnector();
			}
			case CONTAINER -> {
				return new ContainerSnhConnector();
			}
		}
		throw new RuntimeException("Invalid SNH type given.");
	}
}
