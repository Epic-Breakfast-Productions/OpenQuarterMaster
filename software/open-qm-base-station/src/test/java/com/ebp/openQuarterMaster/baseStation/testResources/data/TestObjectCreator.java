package com.ebp.openQuarterMaster.baseStation.testResources.data;

import com.github.javafaker.Faker;

public abstract class TestObjectCreator<T> {

    protected final Faker faker = Faker.instance();

    public abstract T getTestObject();
}
