package tech.ebp.oqm.plugin.mssController.model.exception.module;

public class MssModuleNotFoundException extends Exception {

	public MssModuleNotFoundException(String serialId) {
		super("Module with serial id \"" + serialId + "\" not found.");
	}
}
