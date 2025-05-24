# CS166 Project Phase 3: Airline Management System

## HELPFUL COMMANDS:

for getting into the DB:
psql -h 127.0.0.1 -p 35753 mstei034_project_phase_3_DB

for reestablighing database data:
cs166_db_stop
rm -rf /extra/mstei034/cs166/mydb/data/
source sql/scripts/create_db.sh


## 2. System Requirements & Implemented Features

**User Authentication:**
*   [x] **Create User:** (Stubbed in `AirlineManagement.java`) Allows new users to register.
*   [x] **Log In:** (Stubbed in `AirlineManagement.java`) Allows existing users to log in.

**Management Features (Accessible after Management Login):**
*   [ ] **1. View Flights:** (Stubbed as `feature1`) Display all available flight routes.
*   [ ] **2. View Flight Seats:** (Stubbed as `feature2`) Display seat availability for specific flight instances.
*   [ ] **3. View Flight Status:** (Stubbed as `feature3`) Display the on-time status of flight instances.
*   [ ] **4. View Flights of the day:** (Stubbed as `feature4`) Display all flight instances scheduled for a given day.
*   [ ] **5. View Full Order ID History:** (Stubbed as `feature5`) Display history of all reservations.
    *   *(Note: The stub for feature6 seems to be present but not listed in the menu comments. It might be an additional management feature or a placeholder.)*

**Customer Features (Accessible after Customer Login):**
*   [ ] **10. Search Flights:** (Stubbed in `AirlineManagement.java` but not explicitly linked to a `featureX` method) Allow customers to search for available flights based on criteria (e.g., origin, destination, date).
*   [ ] (Implied) Book Flight / Make Reservation.
*   [ ] (Implied) View Own Reservations.

**Pilot Features (Accessible after Pilot Login):**
*   [ ] **15. Maintenance Request:** (Stubbed in `AirlineManagement.java` but not explicitly linked to a `featureX` method) Allow pilots to submit maintenance requests for planes.

**Technician Features (Accessible after Technician Login):**
*   [ ] (Implied) View Maintenance Requests.
*   [ ] (Implied) Log Completed Repairs.
    *(Note: No explicit menu items for Technicians are stubbed in the current `AirlineManagement.java` menu, but data tables `Technician`, `Repair`, `MaintenanceRequest` support these functionalities.)*

**General:**
*   [ ] Error Handling: Implement robust error handling for invalid inputs, SQL exceptions, and operational errors.
*   [ ] User-friendly CLI: Provide clear prompts and meaningful messages.

## 4. Project Structure

cs166_project_phase3/
├── .DS_Store
├── README.txt
├── data/ # CSV files for database population
│ ├── Customer.csv
│ ├── Flight.csv
│ ├── FlightInstance.csv
│ ├── MaintenanceRequest.csv
│ ├── Pilot.csv
│ ├── Plane.csv
│ ├── Repair.csv
│ ├── Reservation.csv
│ ├── Schedule.csv
│ └── Technician.csv
├── java/ # Java application source and build files
│ ├── classes/ # Compiled .class files
│ │ └── AirlineManagement.class
│ ├── lib/ # JDBC driver
│ │ └── pg73jdbc3.jar
│ ├── scripts/ # Scripts for Java application
│ │ └── compile.sh
│ └── src/ # Java source code
│ └── AirlineManagement.java
└── sql/ # SQL scripts for database setup
├── scripts/ # Scripts for database management
│ └── create_db.sh
└── src/ # SQL source files
├── create_indexes.sql
├── create_tables.sql
└── load_data.sql


## 5. Setup and Execution

### Prerequisites
1.  PostgreSQL server installed and running.
2.  The `cs166_` helper scripts (e.g., `cs166_initdb`, `cs166_createdb`) available in your environment (typical for a CS166 course setup).
3.  Java Development Kit (JDK) 1.8 or higher installed.

### Database Setup
1.  Navigate to the project's root directory (`cs166_project_phase3`).
2.  Run the database creation script:
    ```bash
    source sql/scripts/create_db.sh
    ```
    This script will:
    *   Initialize and start the PostgreSQL database (if not already running).
    *   Create a new database named `[your_username]_project_phase_3_DB`.
    *   Execute `create_tables.sql` to define the schema.
    *   Execute `create_indexes.sql` (currently empty, for potential future use).
    *   Execute `load_data.sql` to populate the tables from the CSV files in the `data/` directory.

### Running the Java Application
1.  Ensure the database is set up and running.
2.  Navigate to the project's root directory.
3.  Run the Java compilation and execution script:
    ```bash
    source java/scripts/compile.sh
    ```
    This script will:
    *   Compile `java/src/AirlineManagement.java` into `java/classes/`.
    *   Run the `AirlineManagement` application, connecting to the database created in the previous step.

## 6. Database Schema

The database schema is defined in `sql/src/create_tables.sql`. It includes the following tables:

*   **`Plane`**: Information about aircraft.
*   **`Flight`**: Information about flight routes.
*   **`Schedule`**: Regular schedules for flights.
*   **`FlightInstance`**: Specific occurrences of flights on particular dates.
*   **`Customer`**: Customer details.
*   **`Reservation`**: Customer reservations for flight instances.
*   **`Technician`**: Information about maintenance technicians.
*   **`Repair`**: Records of repairs performed on planes.
*   **`Pilot`**: Information about pilots.
*   **`MaintenanceRequest`**: Requests for plane maintenance made by pilots.

Relationships are enforced using primary and foreign keys as defined in the `create_tables.sql` script.

## 7. Physical Database Design (Performance Tuning & Indexes)

*   **Primary Key Indexes:** PostgreSQL automatically creates indexes on primary key columns for all tables, which aids in quick lookups based on these keys.
*   **Custom Indexes (`sql/src/create_indexes.sql`):**
    *   Currently, the `create_indexes.sql` file is empty.
    *   **To Do / Potential Improvement:** Based on common query patterns for the implemented features, additional indexes should be considered and added to this file. For example:
        *   On `FlightInstance(FlightNumber, FlightDate)` for quickly finding flight instances.
        *   On `Reservation(CustomerID)` for customers to quickly find their reservations.
        *   On `Reservation(FlightInstanceID)` for quickly finding all reservations for a flight.
        *   On `Flight(DepartureCity, ArrivalCity)` for flight searches.
        *   On `MaintenanceRequest(PlaneID)` or `Repair(PlaneID)`.
    *   The choice of indexes will depend on the specific queries implemented in the Java application and their expected frequency.

## 8. Assumptions

*   The PostgreSQL server is running locally and accessible on the port specified in `java/scripts/compile.sh` (defaults to `$PGPORT` environment variable).
*   The user running the scripts has the necessary permissions to create databases and tables in PostgreSQL.
*   The `cs166_` environment scripts are correctly set up and in the `PATH`.
*   The paths in `sql/src/load_data.sql` to the CSV files (`data/[filename].csv`) are relative to the location where `psql` is executed by the `create_db.sh` script (which is typically the `sql/src/` directory itself, making the paths correct).
*   Some other included so far are that we have no protection against SQL injections, Users can also chose if they are a technician pilot or customer without checking and we also
are adding new people to the database by just adding one to the max ID and we also assume, that Pilot IDs start with P, Technician IDs with a T.



## 10. Potential Extra Credit & Future Enhancements

*   **GUI Design:** Implement a graphical user interface (e.g., using Java Swing or JavaFX) instead of the current CLI for a more user-friendly experience.
*   **Enhanced Error Handling:** Implement comprehensive input validation and user-friendly error messages for all functionalities.
*   **Advanced Search Functionality:**
    *   For customers: multi-city searches, flexible date searches, sorting by price/duration.
    *   For management: advanced reporting and analytics.
*   **User Roles and Permissions:** Implement a more robust role-based access control system within the Java application, ensuring users can only access functionalities relevant to their roles.
*   **Password Hashing:** Securely store user passwords using hashing and salting (currently not implemented in user creation/login stubs).
*   **Transaction Management:** Ensure atomicity for critical operations like booking a flight (e.g., updating `SeatsSold` and creating a `Reservation` should be an atomic transaction).
*   **Additional Features:**
    *   Online check-in.
    *   Seat selection.
    *   Loyalty program management.
    *   Automated notifications (e.g., flight delays, booking confirmations).
*   **Schema Extensions:**
    *   Add a table for `Airport` details.
    *   Add different `CabinClass` options for reservations with varying prices.
    *   Include `Payment` information for reservations.
*   **Dataset Enhancements:** Generate a larger, more diverse dataset for more thorough testing.
*   **Reporting:** Generate reports for management (e.g., popular routes, revenue per flight, plane utilization).

## 11. Phase 3 Grading Rubric Summary

*   **Documentation (10%):** This README and a final project report.
*   **SQL Queries in Client Application (30%):** Correct and efficient implementation of queries supporting the required functionalities.
*   **Physical DB Design (10%):** Use of indexes for performance tuning (beyond default PK indexes).
*   **Extra Credit (20%):** For good GUI, interface enhancements, dataset/schema changes, etc.