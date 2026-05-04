package com.alerts;

public class ManualAlertFactory extends AlertFactory{
    @Override
    public Alert createAlert(String patientId, String condition, long timespamp) {
        return new ManualAlert(patientId,condition,timespamp);
    }
}
