package com.alerts.decorators;

import com.alerts.Alert;

/**
 * Decorator that tags an alert with a priority level.
 *
 * <p>Prepends a priority label (e.g. "HIGH", "CRITICAL") to the condition
 * string so that consuming code or UI layers can immediately see the urgency
 * level without additional parsing.
 */
public class PriorityAlertDecorator extends AlertDecorator {
    private String priorityLevel;

    /**
     * Wraps the given alert with a priority label.
     *
     * @param decoratedAlert the original alert to wrap
     * @param priorityLevel  a human-readable priority label (e.g. "HIGH")
     */
    public PriorityAlertDecorator(Alert decoratedAlert, String priorityLevel) {
        super(decoratedAlert);
        this.priorityLevel = priorityLevel;
    }

    /** Returns the priority level assigned to this alert. */
    public String getPriorityLevel() {
        return priorityLevel;
    }

    /** Returns the condition string prefixed with the priority label. */
    @Override
    public String getCondition() {
        return "[Priority: " + priorityLevel + "] " + decoratedAlert.getCondition();
    }
}