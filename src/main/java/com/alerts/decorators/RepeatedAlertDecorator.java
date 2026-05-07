package com.alerts.decorators;

import com.alerts.Alert;

/**
 * Decorator that records how many times an alert condition has been re-triggered.
 *
 * <p>Appends a repeat count annotation to the condition string so downstream
 * consumers know the alert has been confirmed more than once.
 */
public class RepeatedAlertDecorator extends AlertDecorator {
    private int repeatCount;

    /**
     * Wraps the given alert and tracks how many times it has been repeated.
     *
     * @param decoratedAlert the original alert to wrap
     * @param repeatCount    the number of times the condition has re-triggered
     */
    public RepeatedAlertDecorator(Alert decoratedAlert, int repeatCount) {
        super(decoratedAlert);
        this.repeatCount = repeatCount;
    }

    /** Returns the number of repetitions recorded for this alert. */
    public int getRepeatCount() {
        return repeatCount;
    }

    /** Returns the condition string annotated with the repeat count. */
    @Override
    public String getCondition() {
        return decoratedAlert.getCondition() + " [Repeated: " + repeatCount + " time(s)]";
    }
}
