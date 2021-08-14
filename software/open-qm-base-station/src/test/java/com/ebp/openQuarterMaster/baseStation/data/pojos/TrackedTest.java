package com.ebp.openQuarterMaster.baseStation.data.pojos;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class TrackedTest extends PojoTest<Tracked> {
    @Override
    public Tracked getBasicTestItem() {
        return new Tracked() {
        };
    }

    @Test
    public void testUpdated() {
        Tracked o = this.getBasicTestItem();

        assertFalse(o.getUpdates().isEmpty());
        assertEquals(1, o.getUpdates().size());

        Tracked o2 = o.updated();

        assertSame(o, o2);

        assertEquals(2, o.getUpdates().size());

        assertTrue(o.getUpdates().get(0).isAfter(o.getUpdates().get(1)));
        
        assertEquals(o.getUpdates().get(0), o.getLastUpdated());
    }

}
