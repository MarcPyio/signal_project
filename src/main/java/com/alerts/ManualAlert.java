package com.alerts;

public class ManualAlert extends Alert{
    public ManualAlert(String patientId, String condition, long timestamp){
        super(patientId,condition,timestamp);
    }
}
