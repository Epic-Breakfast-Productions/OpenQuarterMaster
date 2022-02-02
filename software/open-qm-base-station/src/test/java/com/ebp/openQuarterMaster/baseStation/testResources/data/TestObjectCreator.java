package com.ebp.openQuarterMaster.baseStation.testResources.data;

import net.datafaker.Faker;

public abstract class TestObjectCreator<T> {

    protected final Faker faker = Faker.instance();

    public abstract T getTestObject();
}
