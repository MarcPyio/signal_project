package com.alerts;

public class BloodPressureAlertFactory extends AlertFactory{
    @Override
    public Alert createAlert(String patientId, String condition, long timespamp){

        return new BloodPressureAlert(patientId,condition,timespamp);
    }
}
