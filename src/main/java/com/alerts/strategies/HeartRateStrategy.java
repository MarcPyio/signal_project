package com.alerts.strategies;

import com.data_management.Patient;
import com.data_management.PatientRecord;
import java.util.List;

public class HeartRateStrategy implements AlertStrategy {
    @Override
    public boolean checkAlert(Patient patient, List<PatientRecord> records) {
        for (PatientRecord r : records) {
            if (r.getRecordType().equals("HeartRate")) {
                double hr = r.getMeasurementValue();
                if (hr < 50 || hr > 100) return true;
            }
        }
        return false;
    }
}
