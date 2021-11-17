package com.ebp.openQuarterMaster.baseStation.testResources;

import com.ebp.openQuarterMaster.baseStation.testResources.profiles.ExternalAuthTestProfile;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.TestProfile;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.ClassDescriptor;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.ClassOrdererContext;

import java.util.Comparator;
import java.util.Optional;

@Slf4j
public class TestTypeOrder implements ClassOrderer {
    private static final int ORDER_VAL_NOT_RUNNING = 1;
    private static final int ORDER_VAL_DEFAULT = 2;
    private static final int ORDER_VAL_TEST_PROFILE_EXTERNAL_AUTH = 3;
    private static final int ORDER_VAL_TEST_PROFILE_OTHER = 4;

    @Override
    public void orderClasses(ClassOrdererContext context) {
        log.info("Getting order for classes.");
        context.getClassDescriptors().sort(Comparator.comparingInt(TestTypeOrder::getOrderFor));
    }

    private static int getOrderFor(ClassDescriptor classDescriptor) {
        int order = getOrder(classDescriptor);
        log.info("Order for {}: {}", classDescriptor.getTestClass().getName(), order);
        return order;
    }

    private static int getOrder(ClassDescriptor classDescriptor) {
        if (!classDescriptor.findAnnotation(QuarkusTestResource.class).isPresent()) {
            return ORDER_VAL_NOT_RUNNING;
        }

        Optional<TestProfile> testProfileAnnotation = classDescriptor.findAnnotation(TestProfile.class);
        if (testProfileAnnotation.isPresent()) {
            if (ExternalAuthTestProfile.class.equals(testProfileAnnotation.get().value())) {
                return ORDER_VAL_TEST_PROFILE_EXTERNAL_AUTH;
            }
            return ORDER_VAL_TEST_PROFILE_OTHER;
        }
        return ORDER_VAL_DEFAULT;
    }
}
