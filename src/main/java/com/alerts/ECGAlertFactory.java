package com.alerts;

public class ECGAlertFactory extends AlertFactory{
    @Override
    public Alert createAlert(String patientId, String condition, long timespamp){

        return new ECGAlert(patientId,condition,timespamp);
    }
}
