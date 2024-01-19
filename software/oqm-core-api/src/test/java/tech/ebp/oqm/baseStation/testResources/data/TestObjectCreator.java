package tech.ebp.oqm.baseStation.testResources.data;

import net.datafaker.Faker;

public abstract class TestObjectCreator<T> {
	
	protected final Faker faker = new Faker();
	
	public abstract T getTestObject();
}
