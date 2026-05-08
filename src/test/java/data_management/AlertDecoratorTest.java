package data_management;

import com.alerts.BloodPressureAlert;
import com.alerts.Alert;
import com.alerts.decorators.RepeatedAlertDecorator;
import com.alerts.decorators.PriorityAlertDecorator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AlertDecoratorTest {

    @Test
    void testRepeatedAlertDecoratorCondition() {
        Alert base = new BloodPressureAlert("1", "High BP", 1000L);
        RepeatedAlertDecorator repeated = new RepeatedAlertDecorator(base, 3);
        assertTrue(repeated.getCondition().contains("Repeated"));
        assertEquals(3, repeated.getRepeatCount());
    }

    @Test
    void testRepeatedAlertDecoratorDelegatesPatientId() {
        Alert base = new BloodPressureAlert("42", "High BP", 1000L);
        RepeatedAlertDecorator repeated = new RepeatedAlertDecorator(base, 1);
        assertEquals("42", repeated.getPatientId());
    }

    @Test
    void testPriorityAlertDecoratorCondition() {
        Alert base = new BloodPressureAlert("1", "High BP", 1000L);
        PriorityAlertDecorator priority = new PriorityAlertDecorator(base, "CRITICAL");
        assertTrue(priority.getCondition().contains("CRITICAL"));
        assertEquals("CRITICAL", priority.getPriorityLevel());
    }

    @Test
    void testPriorityAlertDecoratorDelegatesTimestamp() {
        Alert base = new BloodPressureAlert("1", "High BP", 5000L);
        PriorityAlertDecorator priority = new PriorityAlertDecorator(base, "HIGH");
        assertEquals(5000L, priority.getTimestamp());
    }
}
