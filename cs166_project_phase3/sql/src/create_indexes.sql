DROP INDEX IF EXISTS idx_schedule_flightnumber;
DROP INDEX IF EXISTS idx_flightinstance_date_flight;
DROP INDEX IF EXISTS idx_reservation_flightinstanceid;
DROP INDEX IF EXISTS idx_reservation_status;
DROP INDEX IF EXISTS idx_customer_name_combo;
DROP INDEX IF EXISTS idx_flight_departure_arrival;
DROP INDEX IF EXISTS idx_repair_plane_date;
DROP INDEX IF EXISTS idx_maintenancerequest_pilotid;

CREATE INDEX idx_schedule_flightnumber ON Schedule(FlightNumber);
CREATE INDEX idx_flightinstance_date_flight ON FlightInstance(FlightDate, FlightNumber);
CREATE INDEX idx_reservation_flightinstanceid ON Reservation(FlightInstanceID);
CREATE INDEX idx_reservation_status ON Reservation(Status);
CREATE INDEX idx_customer_name_combo ON Customer(LastName, FirstName);
CREATE INDEX idx_flight_departure_arrival ON Flight(DepartureCity, ArrivalCity);
CREATE INDEX idx_repair_plane_date ON Repair(PlaneID, RepairDate);
CREATE INDEX idx_maintenancerequest_pilotid ON MaintenanceRequest(PilotID);
