package tech.ebp.oqm.plugin.mssController.moduleInteraction.service;

public class VoiceDisabledException extends IllegalStateException {
	
	public VoiceDisabledException() {
		super("Voice commands are currently disabled by configuration.");
	}
}
