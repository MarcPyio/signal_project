package com.alerts.strategies;

import com.data_management.Patient;
import com.data_management.PatientRecord;
import java.util.List;

public class OxygenSaturationStrategy implements AlertStrategy {
    @Override
    public boolean checkAlert(Patient patient, List<PatientRecord> records) {
        for (PatientRecord r : records) {
            if (r.getRecordType().equals("Saturation")) {
                if (r.getMeasurementValue() < 92) return true;
            }
        }
        return false;
    }
}
