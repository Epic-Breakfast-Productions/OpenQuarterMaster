package tech.ebp.oqm.baseStation.model.objectUpgrade;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.baseStation.model.objectUpgrade.exception.VersionBumperListIncontiguousException;
import tech.ebp.oqm.baseStation.model.objectUpgrade.testUtil.TestObjectVersionBumper;
import tech.ebp.oqm.baseStation.model.objectUpgrade.testUtil.TestVersionable;
import tech.ebp.oqm.baseStation.model.testUtils.BasicTest;

import java.util.Iterator;
import java.util.SortedSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
class ObjectUpgraderTest extends BasicTest {
	
	@Test
	public void testConstructor() throws VersionBumperListIncontiguousException {
		ObjectUpgrader<TestVersionable> upgrader = new ObjectUpgrader<>(
			TestVersionable.class,
			new TestObjectVersionBumper(2),
			new TestObjectVersionBumper(4),
			new TestObjectVersionBumper(3)
		)
		{
		};
		
		SortedSet<ObjectVersionBumper<TestVersionable>> bumpers = upgrader.getVersionBumpers();
		
		Iterator<ObjectVersionBumper<TestVersionable>> it = bumpers.iterator();
		
		assertEquals(
			2,
			it.next().getBumperTo()
		);
		assertEquals(
			3,
			it.next().getBumperTo()
		);
		assertEquals(
			4,
			it.next().getBumperTo()
		);
	}
	
	@Test
	public void testConstructorOneBumper() throws VersionBumperListIncontiguousException {
		ObjectUpgrader<TestVersionable> upgrader = new ObjectUpgrader<>(
			TestVersionable.class,
			new TestObjectVersionBumper(2)
		)
		{
		};
		
		SortedSet<ObjectVersionBumper<TestVersionable>> bumpers = upgrader.getVersionBumpers();
		
		Iterator<ObjectVersionBumper<TestVersionable>> it = bumpers.iterator();
		
		assertEquals(
			2,
			it.next().getBumperTo()
		);
	}
	
	@Test
	public void testConstructorInContiguous() {
		assertThrows(
			VersionBumperListIncontiguousException.class,
			()->
				new ObjectUpgrader<>(
					TestVersionable.class,
					new TestObjectVersionBumper(2),
					new TestObjectVersionBumper(4)
				)
				{
				}
		);
	}
	
	@Test
	public void testConstructorBumpToOne() {
		assertThrows(
			VersionBumperListIncontiguousException.class,
			()->
				new ObjectUpgrader<>(
					TestVersionable.class,
					new TestObjectVersionBumper(1),
					new TestObjectVersionBumper(2)
				)
				{
				}
		);
	}
	
	//TODO:: test upgrade
}