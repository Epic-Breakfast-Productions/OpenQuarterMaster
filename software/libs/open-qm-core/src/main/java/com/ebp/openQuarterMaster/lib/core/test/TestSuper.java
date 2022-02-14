package com.ebp.openQuarterMaster.lib.core.test;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TestOne.class, name = "ONE"),
        @JsonSubTypes.Type(value = TestTwo.class, name = "TWO")
})
public class TestSuper<T> {
    private ClassType type;

    private T value;
}
