package com.ebp.openQuarterMaster.baseStation.testResources;

import org.junit.jupiter.api.ClassDescriptor;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.ClassOrdererContext;

import java.util.Comparator;

public class TestTypeOrder implements ClassOrderer {
    @Override
    public void orderClasses(ClassOrdererContext context) {
        context.getClassDescriptors().sort(Comparator.comparingInt(TestTypeOrder::getOrder));
    }

    private static int getOrder(ClassDescriptor classDescriptor) {
//        if (classDescriptor.findAnnotation(SpringBootTest.class).isPresent()) {
//            return 4;
//        } else if (classDescriptor.findAnnotation(WebMvcTest.class).isPresent()) {
//            return 3;
//        } else if (classDescriptor.findAnnotation(DataJpaTest.class).isPresent()) {
//            return 2;
//        } else {
//            return 1;
//        }
        return 1;
    }
}
