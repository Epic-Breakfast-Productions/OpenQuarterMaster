package com.ebp.openQuarterMaster.baseStation.data.mongo;

import com.ebp.openQuarterMaster.baseStation.testResources.testClasses.RunningServerTest;

public abstract class MongoEntityTest<V extends OurMongoEntity<T>, T> extends RunningServerTest {
    public abstract V getBasicTestEntity();

    public abstract T getBasicTestObj();

}
