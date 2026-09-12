package tech.ebp.oqm.plugin.mssController.model.exception.module;

import lombok.Getter;
import tech.ebp.oqm.plugin.mssController.service.mssConn.connectors.MssConnector;

public class ModuleConnectionNotOkException extends Exception {

	public ModuleConnectionNotOkException(MssConnector connector) {
		super("Module connection to module with serial id \""+connector.getSerialId()+"\" marked as " + connector.getConnState());
	}

}
