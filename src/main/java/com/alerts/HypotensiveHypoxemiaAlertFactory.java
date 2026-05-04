package com.alerts;

public class HypotensiveHypoxemiaAlertFactory extends AlertFactory{
    @Override
    public Alert createAlert(String patientId, String condition, long timespamp) {
        return new HypotensiveHypoxemiaAlert(patientId,condition,timespamp);
    }
}
