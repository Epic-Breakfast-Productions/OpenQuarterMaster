package tech.ebp.oqm.lib.moduleDriver;

import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import java.time.ZonedDateTime;

/**
 * All information we have on a given module.
 */
@Data
public class ModuleInteractionInfo {
	
	/**
	 * The basic information we have about the module.
	 */
	@NonNull
	@NotNull
	private ModuleInfo info;
	
	/**
	 * The state of the module
	 */
	@NonNull
	@NotNull
	private ModuleState state;
	
	/**
	 * If the module was interactable at the last attempt.
	 */
	private boolean online = true;
	
	/**
	 * The first time we interacted with the module.
	 */
	@NonNull
	@NotNull
	@Past
	private ZonedDateTime firstFound;
	
	/**
	 * The last time we had communication with the module.
	 */
	@NonNull
	@NotNull
	@Past
	private ZonedDateTime lastComm;
	
	/**
	 * Shortcut to get the module's serial number from {@link #getInfo()}
	 *
	 * @return The module's serial number.
	 */
	public String getModuleSerial() {
		if (this.getInfo() == null || this.getInfo().getSerialNo() == null) {
			throw new IllegalStateException("No info or serial number held.");
		}
		return this.getInfo().getSerialNo();
	}
}
