package com.ebp.openQuarterMaster.testResources;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.lifecycle.TestDescription;

@Slf4j
public class OurTestDescription implements TestDescription {
	
	private static final String NAME_FORMAT = "%s__%s";
	
	private final String name;
	
	public OurTestDescription(String methodName, String displayName) {
		String dirtyName = String.format(
			NAME_FORMAT,
			methodName,
			displayName
		);
		
		log.info("Dirty name: {}", dirtyName);
		
		//TODO:: improve
		this.name = dirtyName
			.replaceAll("\\[", "-")
			.replaceAll("]", "-")
			.replaceAll("\\W+", "");
		
		log.info("Clean name: {}", this.name);
	}
	
	public OurTestDescription(ExtensionContext context) {
		this(
			context.getTestMethod().get().getName(),
			context.getDisplayName()
		);
	}
	
	public OurTestDescription(TestInfo info) {
		this(
			info.getTestMethod().get().getName(),
			info.getDisplayName()
		);
	}
	
	@Override
	public String getTestId() {
		return null;
	}
	
	@Override
	public String getFilesystemFriendlyName() {
		return this.name;
	}
}
