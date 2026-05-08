package com.alerts.strategies;

import com.data_management.Patient;
import com.data_management.PatientRecord;
import java.util.List;

public class BloodPressureStrategy implements AlertStrategy {
    @Override
    public boolean checkAlert(Patient patient, List<PatientRecord> records) {
        for (PatientRecord r : records) {
            if (r.getRecordType().equals("SystolicPressure")) {
                if (r.getMeasurementValue() > 180 || r.getMeasurementValue() < 90) return true;
            }
            if (r.getRecordType().equals("DiastolicPressure")) {
                if (r.getMeasurementValue() > 120 || r.getMeasurementValue() < 60) return true;
            }
        }
        return false;
    }
}
