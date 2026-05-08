package com.alerts.strategies;

import com.data_management.Patient;
import com.data_management.PatientRecord;
import java.util.List;

public interface AlertStrategy {
    boolean checkAlert(Patient patient, List<PatientRecord> records);
}
