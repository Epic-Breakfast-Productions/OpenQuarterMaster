package com.ebp.openQuarterMaster.lib.core;

import com.ebp.openQuarterMaster.lib.core.storage.Capacity;
import com.ebp.openQuarterMaster.lib.core.storage.StorageBlock;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.javafaker.Faker;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.units.indriya.AbstractUnit;
import tech.units.indriya.quantity.Quantities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class MainObjectTest {

    @NoArgsConstructor
    private static class TestMainObject extends MainObject {
        public TestMainObject(ObjectId objectId, Map<String, String> atts){
            super(objectId, atts);
        }
    }

    private static Stream<Arguments> jsonTestArgs(){
        return Stream.of(
                Arguments.of(new TestMainObject()),
                Arguments.of(new TestMainObject(null, new HashMap<>(){{put("hello","world");}})),
                Arguments.of(new TestMainObject(ObjectId.get(), null)),
                Arguments.of(new TestMainObject(ObjectId.get(), new HashMap<>(){{put("hello","world");}}))
        );
    }

    @ParameterizedTest(name = "jsonTest[{index}]")
    @MethodSource("jsonTestArgs")
    public void jsonTest(TestMainObject testMainObject) throws JsonProcessingException {
        String storedJson = Utils.OBJECT_MAPPER.writeValueAsString(testMainObject);

        log.info("Test Main Object object: {}", testMainObject);
        log.info("Test Main Object json: {}", storedJson);

        TestMainObject deserialized = Utils.OBJECT_MAPPER.readValue(storedJson, TestMainObject.class);

        assertEquals(testMainObject, deserialized, "Deserialized object was not equal to original.");
    }

}