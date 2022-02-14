package com.ebp.openQuarterMaster.lib.core.storage.stored;


import com.ebp.openQuarterMaster.lib.core.Utils;
import com.ebp.openQuarterMaster.lib.core.testUtils.BasicTest;
import com.ebp.openQuarterMaster.lib.core.validation.validators.StoredValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.units.indriya.AbstractUnit;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import javax.measure.Unit;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class StoredTest extends BasicTest {
    private static Stream<Arguments> jsonTestArgs(){
        return Stream.of(
                Arguments.of(new Stored(Quantities.getQuantity(50, AbstractUnit.ONE))),
                Arguments.of(new Stored(new HashMap<>())),
                Arguments.of(
                        new Stored(
                                new HashMap<>(){{
                                    put(FAKER.idNumber().valid(), new TrackedItem());
                                }}
                        )
                )
        );
    }

    private StoredValidator validator;

    @BeforeEach
    public void setUp() {
        validator = new StoredValidator();
    }

    @ParameterizedTest(name = "jsonTest[{index}]")
    @MethodSource("jsonTestArgs")
    public void jsonTest(Stored testStored) throws JsonProcessingException {
        String storedJson = Utils.OBJECT_MAPPER.writeValueAsString(testStored);

        log.info("Stored object: {}", testStored);
        log.info("Stored json: {}", storedJson);

        Stored deserialized = Utils.OBJECT_MAPPER.readValue(storedJson, Stored.class);

        assertEquals(testStored, deserialized, "Deserialized object was not equal to original.");
    }

    @Test
    public void constructorAmountQuantityDecomposedTest(){
        Stored s = new Stored(1, AbstractUnit.ONE);

        assertEquals(StoredType.AMOUNT, s.getType());
        assertEquals(1, s.getAmount().getValue());
        assertEquals(AbstractUnit.ONE, s.getAmount().getUnit());
        assertNull(s.getItems());
        assertTrue(
                validator.isValid(s, null)
        );
    }
    @Test
    public void constructorAmountQuantityNullDecomposedTest(){
        assertThrows(NullPointerException.class, () -> {new Stored(1, null);});
        assertThrows(NullPointerException.class, () -> {new Stored(null, AbstractUnit.ONE);});
    }

    @Test
    public void constructorAmountQuantityTest(){
        Stored s = new Stored(Quantities.getQuantity(1, AbstractUnit.ONE));

        assertEquals(StoredType.AMOUNT, s.getType());
        assertEquals(1, s.getAmount().getValue());
        assertEquals(AbstractUnit.ONE, s.getAmount().getUnit());
        assertNull(s.getItems());
        assertTrue(validator.isValid(s, null));
    }
    @Test
    public void constructorAmountNullQuantityTest(){
        assertThrows(NullPointerException.class, () -> {new Stored((Quantity) null);});
    }

    @Test
    public void constructorAmountUnitTest(){
        Stored s = new Stored(AbstractUnit.ONE);

        assertEquals(StoredType.AMOUNT, s.getType());
        assertEquals(0, s.getAmount().getValue());
        assertEquals(AbstractUnit.ONE, s.getAmount().getUnit());
        assertNull(s.getItems());
        assertTrue(validator.isValid(s, null));
    }

    @Test
    public void constructorAmountNullUnitTest(){
        assertThrows(NullPointerException.class, () -> {new Stored((Unit) null);});
    }

    @Test
    public void constructorTrackedHashMapTest(){
        Map<String, TrackedItem> trackedMap = new HashMap<>(){{
            put(
                    FAKER.idNumber().valid(),
                    new TrackedItem()
            );
        }};
        Stored s = new Stored(trackedMap);

        assertEquals(StoredType.TRACKED, s.getType());
        assertSame(trackedMap, s.getItems());
        assertNotNull(s.getAmount());
        assertTrue(validator.isValid(s, null));
    }

    @Test
    public void addAmountTest(){
        int valOne = 3;
        int valTwo = 4;

        Stored s1 = new Stored(Quantities.getQuantity(valOne, AbstractUnit.ONE));

        Stored s2 = s1.add(Quantities.getQuantity(valTwo, AbstractUnit.ONE));

        assertSame(s1, s2);
        assertEquals(
                Quantities.getQuantity(valOne + valTwo, AbstractUnit.ONE),
                s2.getAmount()
        );
    }
    @Test
    public void addAmountToTrackedTest(){
        Stored s = new Stored(new HashMap<>());
        assertThrows(IllegalStateException.class, () -> {
            s.add(Quantities.getQuantity(1, AbstractUnit.ONE));
        });
    }

    @Test
    public void addItemsTest(){
        String key = "hello";
        TrackedItem val = new TrackedItem();

        Stored s1 = new Stored(new HashMap<>());

        Stored s2 = s1.add(new HashMap<>(){{put(key, val);}});

        assertSame(s1, s2);
        assertEquals(1, s1.getItems().size());
        assertTrue(s1.getItems().containsKey(key));
        assertSame(val, s1.getItems().get(key));
    }
    @Test
    public void addItemTest(){
        String key = "hello";
        TrackedItem val = new TrackedItem();

        Stored s1 = new Stored(new HashMap<>());

        Stored s2 = s1.add(key, val);

        assertSame(s1, s2);
        assertEquals(1, s1.getItems().size());
        assertTrue(s1.getItems().containsKey(key));
        assertSame(val, s1.getItems().get(key));
    }

    @Test
    public void addItemsToAmountTest(){
        Stored s = new Stored(Quantities.getQuantity(1, AbstractUnit.ONE));
        assertThrows(IllegalStateException.class, () -> {
            s.add(new HashMap<>());
        });
    }
    @Test
    public void addItemToAmountTest(){
        Stored s = new Stored(Quantities.getQuantity(1, AbstractUnit.ONE));
        assertThrows(IllegalStateException.class, () -> {
            s.add("hello", new TrackedItem());
        });
    }

    @Test
    public void addStoredTypeMismatchTest(){
        Stored tracked = new Stored(new HashMap<>());
        Stored amount = new Stored(Quantities.getQuantity(1, AbstractUnit.ONE));
        assertThrows(IllegalStateException.class, () -> {tracked.add(amount);});
        assertThrows(IllegalStateException.class, () -> {amount.add(tracked);});
    }

    @Test
    public void addStoredAmountTest(){
        int valOne = 3;
        int valTwo = 4;

        Stored s1 = new Stored(Quantities.getQuantity(valOne, AbstractUnit.ONE));

        Stored s2 = new Stored(Quantities.getQuantity(valTwo, AbstractUnit.ONE));

        Stored s3 = s1.add(s2);

        assertSame(s1, s3);
        assertEquals(
                Quantities.getQuantity(valOne + valTwo, AbstractUnit.ONE),
                s3.getAmount()
        );
    }
    @Test
    public void addStoredItemsTest(){
        String key = "hello";
        TrackedItem val = new TrackedItem();

        Stored s1 = new Stored(new HashMap<>());
        Stored s2 = new Stored(new HashMap<>(){{put(key, val);}});

        Stored s3 = s1.add(s2);

        assertSame(s1, s3);
        assertEquals(1, s1.getItems().size());
        assertTrue(s1.getItems().containsKey(key));
        assertSame(val, s1.getItems().get(key));
    }

    //TODO:: test equals
}