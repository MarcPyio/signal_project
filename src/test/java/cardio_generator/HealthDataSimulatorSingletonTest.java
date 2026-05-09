package cardio_generator;

import com.cardio_generator.HealthDataSimulator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class HealthDataSimulatorSingletonTest {

    @Test
    public void testOnlyOneInstanceExists() {
        //Attempt to grab the Singleton instance twice
        HealthDataSimulator instance1 = HealthDataSimulator.getInstance();
        HealthDataSimulator instance2 = HealthDataSimulator.getInstance();

        // Prove that they are not just identical, but the EXACT same object
        assertSame(instance1, instance2, "Both variables should point to the exact same Singleton instance in memory.");
    }

    @Test
    public void testSharedState() {
        HealthDataSimulator instance1 = HealthDataSimulator.getInstance();
        HealthDataSimulator instance2 = HealthDataSimulator.getInstance();

        instance1.testVariable = 10;

        assertSame(instance1.testVariable,instance2.testVariable,"variable should be shared across the Singleton.");
    }
}
