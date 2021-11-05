package com.ebp.openQuarterMaster.lib.core.testUtils;

import javax.validation.ClockProvider;
import javax.validation.ConstraintValidatorContext;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class TestConstraintValidatorContext implements ConstraintValidatorContext {

    @Getter
    private final List<String> errorMessages = new ArrayList<>();

    @Override
    public void disableDefaultConstraintViolation() {

    }

    @Override
    public String getDefaultConstraintMessageTemplate() {
        return null;
    }

    @Override
    public ClockProvider getClockProvider() {
        return null;
    }

    @Override
    public ConstraintViolationBuilder buildConstraintViolationWithTemplate(String messageTemplate) {
        TestConstraintValidatorContext me = this;
        return new ConstraintViolationBuilder() {
            String message = messageTemplate;
            TestConstraintValidatorContext parent = me;

            @Override
            public NodeBuilderDefinedContext addNode(String name) {
                return null;
            }

            @Override
            public NodeBuilderCustomizableContext addPropertyNode(String name) {
                return null;
            }

            @Override
            public LeafNodeBuilderCustomizableContext addBeanNode() {
                return null;
            }

            @Override
            public ContainerElementNodeBuilderCustomizableContext addContainerElementNode(String name, Class<?> containerType, Integer typeArgumentIndex) {
                return null;
            }

            @Override
            public NodeBuilderDefinedContext addParameterNode(int index) {
                return null;
            }

            @Override
            public ConstraintValidatorContext addConstraintViolation() {
                parent.getErrorMessages().add(this.message);
                return parent;
            }
        };
    }

    @Override
    public <T> T unwrap(Class<T> type) {
        return null;
    }
}
