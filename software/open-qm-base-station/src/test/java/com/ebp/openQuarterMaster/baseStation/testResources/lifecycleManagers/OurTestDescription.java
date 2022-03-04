package com.ebp.openQuarterMaster.baseStation.testResources.lifecycleManagers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.TestInfo;
import org.testcontainers.lifecycle.TestDescription;

@Slf4j
public class OurTestDescription implements TestDescription {
	
	private static final String NAME_FORMAT = "%s__%s";
	
	private final String name;
	
	public OurTestDescription(TestInfo info){
		String nameShort = info.getTestMethod().get().getName();
		
		String dirtyName = String.format(
			NAME_FORMAT,
			nameShort,
			info.getDisplayName()
		);
		
		log.info("Dirty name: {}", dirtyName);
		
		//TODO:: improve
		this.name = dirtyName
						.replaceAll("\\[","-")
						.replaceAll("]","-")
						.replaceAll("\\W+", "");
		
		log.info("Clean name: {}", this.name);
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
