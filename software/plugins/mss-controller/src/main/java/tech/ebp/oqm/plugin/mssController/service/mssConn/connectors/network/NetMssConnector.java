package tech.ebp.oqm.plugin.mssController.service.mssConn.connectors.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.validation.Validator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.plugin.mssController.config.ModuleConfig;
import tech.ebp.oqm.plugin.mssController.model.exception.module.ModuleSetupFailedException;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.Command;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.response.CommandResponse;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.message.Message;
import tech.ebp.oqm.plugin.mssController.service.mssConn.connectors.MssConnector;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Getter(AccessLevel.PRIVATE)
@Slf4j
public class NetMssConnector extends MssConnector {

	private final MssModuleRestClient restClient;
	private final String url;
	private final String authorization;
	@Getter
	private Queue<Message> incomingMessages = new ConcurrentLinkedQueue<>();

	public NetMssConnector(
		Validator validator,
		ObjectMapper mapper,
		EventBus eventBus,
		ModuleConfig.NetConfig.NetModuleConfig netModuleConfig,
		MssModuleRestClient restClient
	) throws ModuleSetupFailedException {
		this.restClient = restClient;
		this.url = netModuleConfig.url();

		this.authorization =
			Base64.getEncoder()
				.encodeToString(
					(netModuleConfig.serialId() + ":" + netModuleConfig.secret().orElse(""))
						.getBytes(StandardCharsets.UTF_8)
				);

		super(validator, mapper, eventBus, netModuleConfig);

		if (!this.getModuleInfo().getSerialId().equals(netModuleConfig.serialId())) {
			throw new ModuleSetupFailedException("Serial id of module config does not match module info.");
		}
	}

	@Override
	protected CommandResponse sendCommandImpl(Command command) {
		return this.getRestClient().sendCommand(
			this.getUrl(),
			this.getAuthorization(),
			command
		);
	}
}
