/*
 * Copyright (c) 2022, Indunil Rathnayake. All Rights Reserved.
 */

package com.aips.traffic.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test the functionality of TrafficDataEntry
 */
public class TrafficDataEntryTest {

    @Test
    public void testSetAndGetTrafficDataEntry() {

        TrafficDataEntry trafficDataEntry = new TrafficDataEntry("2021-12-01T05:00:30", "5");
        assertEquals(5, trafficDataEntry.getNumOfCars());
        assertEquals("2021-12-01T05:00:30", trafficDataEntry.getDateTime().toString());
        assertEquals("2021-12-01", trafficDataEntry.getDate().toString());
    }
}
