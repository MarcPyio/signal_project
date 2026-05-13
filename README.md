# Project Members in Group 67
- Student ID: 6429774
- Student ID: 6435667

# Cardio Data Simulator

# UML_models-documentation

Data Storage System

The Data Storage System is responsible for storing and managing all patient data. The main class, DataStorage, keeps track of patients using a map where each patient ID is linked to a Patient object. Each Patient contains a list of PatientRecord objects, where each record represents one measurement at a specific time, such as heart rate or blood pressure. New data is added using the addPatientData method, which creates a new patient if needed or updates an existing one. Data can be retrieved using the getRecords method, which allows filtering by patient ID and time range. This is useful for both real-time monitoring and looking at past data. The system also includes an AccessController to make sure only authorized users can access sensitive data. The DataRetriever class is used by medical staff to request data in a controlled way. A RetentionPolicy is used to remove old data after a certain time, helping manage storage. An AuditLog records who accesses the data, which improves security and traceability. Overall, the system is designed to be secure, organized, and easy to use.

Data Access Layer

The Data Access Layer is responsible for receiving data from external sources and preparing it for use in the system. The DataListener interface defines how data is received, and there are different implementations such as TCPDataListener, WebSocketDataListener, and FileDataListener. This allows the system to work with different data sources without changing other parts of the system. Once the data is received, it is passed to the DataParser, which converts the raw data into a structured format called PatientRecord. This ensures that all data is consistent and easy to use. The DataSourceAdapter connects everything together by reading data from a listener, sending it to the parser, and then storing it in DataStorage. The DataReader interface provides a general way to load data into the system. Each component has a clear responsibility, which keeps the system simple and flexible. This design makes it easy to add new data sources in the future without affecting the rest of the system. Overall, the Data Access Layer ensures smooth and reliable data flow into the system.

Alert Generation System

The Alert Generation System is responsible for checking patient data and creating alerts when something is wrong. The main class, AlertGenerator, gets patient data from DataStorage and compares it with rules stored in ThresholdRepository. These rules define safe limits for different health measurements, such as heart rate or blood pressure, and can be different for each patient. When a value goes outside the safe range, the system creates an Alert object that includes the patient ID, the problem, and the time it happened. This alert is then sent to the AlertManager, which is responsible for notifying medical staff. The AlertManager uses a NotificationChannel so alerts can be sent in different ways, such as messages or system notifications. Each class has a clear responsibility, which makes the system easy to understand and maintain. For example, AlertGenerator handles checking data, while AlertManager handles sending alerts. This design also makes it easy to extend the system later, such as adding new alert rules or new ways to notify staff. Overall, the system ensures that important health issues are detected quickly and handled properly.

Patient Identification System

The Patient Identification System makes sure that incoming data is connected to the correct patient. This is very important because using the wrong patient information could lead to serious problems. The main class, PatientIdentifier, receives incoming data and tries to match it with a real hospital patient. It uses the IdentityManager, which stores the mapping between simulator IDs and hospital patient IDs. Once a match is found, the system retrieves detailed information about the patient from the HospitalPatientRepository, such as their name and medical history. If no match is found or something seems wrong, the AnomalyHandler handles the problem. It can flag the issue, isolate the data, and notify an administrator so it can be checked. Each part of the system has a clear role, which makes it easier to manage and maintain. This design also helps protect sensitive patient information by limiting access to only the necessary parts of the system. Overall, the system ensures that all incoming data is correctly linked to the right patient, which is essential for accurate monitoring and safe decision-making in a healthcare environment.

## Features

- Simulate real-time ECG, blood pressure, blood saturation, and blood levels data.
- Supports multiple output strategies:
  - Console output for direct observation.
  - File output for data persistence.
  - WebSocket and TCP output for networked data streaming.
- Configurable patient count and data generation rate.
- Randomized patient ID assignment for simulated data diversity.

## Getting Started

### Prerequisites

- Java JDK 11 or newer.
- Maven for managing dependencies and compiling the application.

### Installation

1. Clone the repository:

   ```sh
   git clone https://github.com/tpepels/signal_project.git
   ```

2. Navigate to the project directory:

   ```sh
   cd signal_project
   ```

3. Compile and package the application using Maven:
   ```sh
   mvn clean package
   ```
   This step compiles the source code and packages the application into an executable JAR file located in the `target/` directory.

### Running the Simulator

After packaging, you can run the simulator directly from the executable JAR:

```sh
java -jar target/cardio_generator-1.0-SNAPSHOT.jar
```

To run with specific options (e.g., to set the patient count and choose an output strategy):

```sh
java -jar target/cardio_generator-1.0-SNAPSHOT.jar --patient-count 100 --output file:./output
```

### Supported Output Options

- `console`: Directly prints the simulated data to the console.
- `file:<directory>`: Saves the simulated data to files within the specified directory.
- `websocket:<port>`: Streams the simulated data to WebSocket clients connected to the specified port.
- `tcp:<port>`: Streams the simulated data to TCP clients connected to the specified port.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
