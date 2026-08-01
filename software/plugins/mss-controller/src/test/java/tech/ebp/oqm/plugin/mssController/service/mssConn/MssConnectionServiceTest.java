package tech.ebp.oqm.plugin.mssController.service.mssConn;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.ResourceArg;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.commands.GetModuleStateCommand;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.response.CommandResponse;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.state.ModuleState;
import tech.ebp.oqm.plugin.mssController.service.mssConn.connectors.MssConnector;
import tech.ebp.oqm.plugin.mssController.service.mssConn.connectors.serial.SerialMssConnector;
import tech.ebp.oqm.plugin.mssController.testResources.modules.TestModuleResource;
import tech.ebp.oqm.plugin.mssController.testResources.testClasses.RunningServerTest;

import static org.junit.jupiter.api.Assertions.*;
import static tech.ebp.oqm.plugin.mssController.model.utils.JacksonUtils.OBJECT_MAPPER;

@Slf4j
@QuarkusTest
@QuarkusTestResource(
	value = TestModuleResource.class,
	restrictToAnnotatedClass = true,
	initArgs = {
		@ResourceArg(name = TestModuleResource.NUM_SERIAL_MODULE_RES_NAME, value = "1")
	}
)
class MssConnectionServiceTest extends RunningServerTest {

	@Inject
	MssConnectionService mssConnectionService;

	@Test
	public void testSetup() throws Exception {
		assertEquals(0, this.mssConnectionService.getModuleSetupFailedExceptions().size());
		assertEquals(1, this.mssConnectionService.getActiveConnections().size());

		MssConnector connector = this.mssConnectionService.getActiveConnections().get(TestModuleResource.getModules().getFirst().getModuleId());

		assertEquals(SerialMssConnector.class, connector.getClass());

		CommandResponse response = connector.sendCommand(new GetModuleStateCommand());

		log.info("Response: {}", response);

		assertEquals(
			TestModuleResource.getModules().getFirst().getModuleState(),
			OBJECT_MAPPER.treeToValue(response.getResponse(), ModuleState.class)
		);
	}

	@Test
	public void testGetConnector() throws Exception {
		MssConnector conn = this.mssConnectionService.getConnector(TestModuleResource.getModules().getFirst().getModuleId());

		assertNotNull(conn);
		assertEquals(TestModuleResource.getModules().getFirst().getModuleId(), conn.getSerialId());
	}
}
