package com.ebp.openQuarterMaster.plugin.moduleInteraction.service;

public class VoiceDisabledException extends IllegalStateException {
	
	public VoiceDisabledException() {
		super("Voice commands are currently disabled by configuration.");
	}
}
