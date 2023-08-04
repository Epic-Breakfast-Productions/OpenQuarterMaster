package tech.ebp.oqm.baseStation.model.objectUpgrade;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.baseStation.model.object.Versionable;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class UpgradeResultTest {
	
	@Test
	public void testWasUpgraded() {
		assertTrue(
			new UpgradeResult<>(
				()->2,
				Duration.ZERO,
				1
			).wasUpgraded()
		);
	}
	
	@Test
	public void testWasNotUpgraded() {
		assertFalse(
			new UpgradeResult<>(
				()->1,
				Duration.ZERO,
				1
			).wasUpgraded()
		);
	}
}