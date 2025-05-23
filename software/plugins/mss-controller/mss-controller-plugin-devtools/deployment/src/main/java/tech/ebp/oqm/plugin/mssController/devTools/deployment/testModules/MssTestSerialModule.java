package tech.ebp.oqm.plugin.mssController.devTools.deployment.testModules;

import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.SelinuxContext;
import tech.ebp.oqm.plugin.mssController.lib.command.response.ModuleInfo;

public class MssTestSerialModule extends MssTestModule<MssTestSerialModule> {
	private static final String HOST_SERIAL_DIR = "/tmp/oqm/mss-test/serialModules/";

	public MssTestSerialModule(ModuleInfo moduleInfo) {
		super(moduleInfo);
	}

	@Override
	protected void configure() {
		addEnv("moduleConfig.type", "SERIAL");
		addFileSystemBind(
			HOST_SERIAL_DIR,
			"/dev/pts/",
			BindMode.READ_WRITE,
			SelinuxContext.NONE
		);

		this.appConfig.put("moduleConfig.serial.scanSerial", "true");
		this.appConfig.put("moduleConfig.serial.scanDir", HOST_SERIAL_DIR);
		//TODO:: do? How? even possible?
//		addEnv("moduleConfig.serial.modules[0].portPath", "GET");

		super.configure();
	}
}
