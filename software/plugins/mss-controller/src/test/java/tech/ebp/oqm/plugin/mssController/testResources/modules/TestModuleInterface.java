package tech.ebp.oqm.plugin.mssController.testResources.modules;

import java.util.Optional;

public abstract class TestModuleInterface implements AutoCloseable {

	public abstract void send(String message);

	public abstract Optional<String> receive();
}
