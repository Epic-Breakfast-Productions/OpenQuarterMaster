package tech.ebp.oqm.plugin.mssController.service.mssConn.serial;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.plugin.mssController.config.ModuleConfig;
import tech.ebp.oqm.plugin.mssController.model.exception.ModuleSetupFailedException;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.Command;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.response.CommandResponse;
import tech.ebp.oqm.plugin.mssController.service.mssConn.MssConnector;

import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class SerialMssConnector extends MssConnector {

	private final ReentrantLock lock = new ReentrantLock();
	private final ModuleConfig.SerialConfig.SerialModuleConfig moduleConfig;

	public SerialMssConnector(
		ObjectMapper mapper,
		ModuleConfig.SerialConfig.SerialModuleConfig moduleConfig
	) throws ModuleSetupFailedException {
		this.moduleConfig = moduleConfig;

		super(mapper);
	}

	@Override
	protected CommandResponse sendCommandImpl(@Valid Command command) {
		//TODO:: this
		return null;
	}
}
