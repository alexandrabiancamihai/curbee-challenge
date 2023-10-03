package org.curbee.auditsystem;

import org.curbee.auditsystem.exception.AuditException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DiffToolTest {

    @Test
    void testPropertyUpdate() {
        SampleClass1 prev = new SampleClass1("James", null);
        SampleClass1 curr = new SampleClass1("Jim", null);

         DiffTool<SampleClass1> diffTool = new DiffTool<>();

        List<ChangeType> diffs = diffTool.diff(prev, curr);

        assertEquals(1, diffs.size());
        assertTrue(diffs.get(0) instanceof PropertyUpdate);

        PropertyUpdate update = (PropertyUpdate) diffs.get(0);
        assertEquals("firstName", update.getProperty());
        assertEquals("James", update.getPrevious());
        assertEquals("Jim", update.getCurrent());
    }

    @Test
    void testNestedPropertyUpdate() {
        SampleClass1 prev = new SampleClass1("James", new Subscription("ACTIVE"));
        SampleClass1 curr = new SampleClass1("James", new Subscription("EXPIRED"));
        DiffTool<SampleClass1> diffTool = new DiffTool<>();

        List<ChangeType> diffs = diffTool.diff(prev, curr);

        assertEquals(1, diffs.size());
        assertTrue(diffs.get(0) instanceof PropertyUpdate);

        PropertyUpdate update = (PropertyUpdate) diffs.get(0);
        assertEquals("subscription.status", update.getProperty());
        assertEquals("ACTIVE", update.getPrevious());
        assertEquals("EXPIRED", update.getCurrent());
    }

    @Test
    void testListAdditionsAndRemovals() {
        SampleClass1 prev = new SampleClass1("James", null, List.of("Interior/Exterior Wash"));
        SampleClass1 curr = new SampleClass1("James", null, List.of("Oil Change"));
        DiffTool<SampleClass1> diffTool = new DiffTool<>();

        List<ChangeType> diffs = diffTool.diff(prev, curr);

        assertEquals(1, diffs.size());
        assertTrue(diffs.get(0) instanceof ListUpdate);

        ListUpdate update = (ListUpdate) diffs.get(0);

        assertEquals("services", update.getProperty());
        assertTrue(update.getAdded().contains("Oil Change"));
        assertTrue(update.getRemoved().contains("Interior/Exterior Wash"));
    }

    @Test
    void testPropertyUpdateWithinList() {
        Vehicle vehiclePrev = new Vehicle("v_1", "My Car");
        Vehicle vehicleCurr = new Vehicle("v_1", "23 Ferrari 296 GTS");
        SampleClass prev = new SampleClass(List.of(vehiclePrev));
        SampleClass curr = new SampleClass(List.of(vehicleCurr));

        DiffTool<SampleClass> diffTool1 = new DiffTool<>();

        List<ChangeType> diffs = diffTool1.diff(prev, curr);

        assertEquals(1, diffs.size());
        assertTrue(diffs.get(0) instanceof PropertyUpdate);

        PropertyUpdate update = (PropertyUpdate) diffs.get(0);

        assertEquals("vehicles[v_1].displayName", update.getProperty());
        assertEquals("My Car", update.getPrevious());
        assertEquals("23 Ferrari 296 GTS", update.getCurrent());
    }

    @Test
    void testAddedItemInList() {
        Vehicle vehiclePrev = new Vehicle("v_1", "My Car");
        Vehicle vehicleCurr1 = new Vehicle("v_1", "My Car");
        Vehicle vehicleCurr2 = new Vehicle("v_2", "My Second Car");
        SampleClass prev = new SampleClass(List.of(vehiclePrev));
        SampleClass curr = new SampleClass(Arrays.asList(vehicleCurr1, vehicleCurr2));
        DiffTool<SampleClass> diffTool = new DiffTool<>();

        List<ChangeType> diffs = diffTool.diff(prev, curr);

        assertEquals(1, diffs.size());
        assertTrue(diffs.get(0) instanceof PropertyUpdate);

        PropertyUpdate update = (PropertyUpdate) diffs.get(0);
        assertEquals("vehicles[v_2]", update.getProperty());
        assertNull(update.getPrevious());
        assertEquals(vehicleCurr2, update.getCurrent());
    }

    @Test
    void testRemovedItemInList() {
        Vehicle vehiclePrev1 = new Vehicle("v_1", "My Car");
        Vehicle vehiclePrev2 = new Vehicle("v_2", "My Second Car");
        Vehicle vehicleCurr = new Vehicle("v_1", "My Car");
        SampleClass prev = new SampleClass(Arrays.asList(vehiclePrev1, vehiclePrev2));
        SampleClass curr = new SampleClass(List.of(vehicleCurr));
        DiffTool<SampleClass> diffTool = new DiffTool<>();

        List<ChangeType> diffs = diffTool.diff(prev, curr);

        assertEquals(1, diffs.size());
        assertTrue(diffs.get(0) instanceof PropertyUpdate);

        PropertyUpdate update = (PropertyUpdate) diffs.get(0);

        assertEquals("vehicles[v_2]", update.getProperty());
        assertEquals(vehiclePrev2, update.getPrevious());
        assertNull(update.getCurrent());
    }

    @Test
    void testMissingAuditKey()  {
        SampleClass2 prev = new SampleClass2(List.of(new VehicleWithoutId("My Car")));
        SampleClass2 curr = new SampleClass2(List.of(new VehicleWithoutId("23 Ferrari 296 GTS")));
        DiffTool<SampleClass2> diffTool = new DiffTool<>();

        Exception exception = assertThrows(AuditException.class, () -> diffTool.diff(prev, curr));

        assertTrue(exception.getMessage().contains("The audit system lacks the information it needs to determine what has changed."));
    }

    static class Vehicle {
        @AuditKey
        String id;
        String displayName;

        public Vehicle(String id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }
    }

    static class VehicleWithoutId {
        String displayName;

        public VehicleWithoutId(String displayName) {
            this.displayName = displayName;
        }
    }

    static class SampleClass {
        List<Vehicle> vehicles;

        public SampleClass(List<Vehicle> vehicles) {
            this.vehicles = vehicles;
        }
    }

    static class SampleClass2 {
        List<VehicleWithoutId> vehicles;

        public SampleClass2(List<VehicleWithoutId> vehicles) {
            this.vehicles = vehicles;
        }
    }

    static class SampleClass1 {
        String firstName;
        Subscription subscription;
        List<String> services;

        public SampleClass1(String firstName, Subscription subscription) {
            this.firstName = firstName;
            this.subscription = subscription;
        }

        public SampleClass1(String firstName, Subscription subscription, List<String> services) {
            this.firstName = firstName;
            this.subscription = subscription;
            this.services = services;
        }
    }

    static class Subscription {
        String status;

        public Subscription(String status) {
            this.status = status;
        }
    }
}
