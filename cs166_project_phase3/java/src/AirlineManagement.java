/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;
import java.io.IOException;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class AirlineManagement {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of AirlineManagement
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public AirlineManagement(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end AirlineManagement

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            AirlineManagement.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      AirlineManagement esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the AirlineManagement object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new AirlineManagement (dbname, dbport, user, "");
         String authorisedUser = null;
         boolean keepon = true;
         while (keepon) {
            if (authorisedUser == null) {
                System.out.println("\n========= AIRLINE SYSTEM LOGIN =========");
                System.out.println("1. Create User Account");
                System.out.println("2. Log In");
                System.out.println("9. Exit Application");
                System.out.println("======================================");
                switch (readChoice()) {
                    case 1: CreateUser(esql); break;
                    case 2: authorisedUser = LogIn(esql); break;
                    case 9: keepon = false; break;
                    default: System.out.println("Unrecognized choice!"); break;
                }
            } else {
                String userRole = "";
                if (authorisedUser.startsWith("CUSTOMER_")) {
                    userRole = "Customer";
                } else if (authorisedUser.startsWith("PILOT_")) {
                    userRole = "Pilot";
                } else if (authorisedUser.startsWith("TECHNICIAN_")) {
                    userRole = "Technician";
                }
                System.out.println("\n========= " + userRole.toUpperCase() + " MENU =========");
                System.out.println("Logged in as: " + authorisedUser);
                System.out.println("--------------------------------------");

                if (userRole.equals("Customer")) {
                    System.out.println("1. View All Flights (by origin/destination)");
                    System.out.println("2. View Flight Seat Availability");
                    System.out.println("3. View Flight On-Time Status");
                    System.out.println("4. View All Flights for a Specific Date");
                    System.out.println("5. View My Reservation History");
                    System.out.println("6. Search Available Flights (by criteria)");
                    System.out.println("7. Book a Flight / Make Reservation");
                    System.out.println("8. View My Current Reservations");
                } else if (userRole.equals("Pilot")) {
                    System.out.println("1. View My Assigned Flights for Today/Date");
                    System.out.println("2. View Flight Manifest (Passenger List)");
                    System.out.println("3. Submit Maintenance Request");
                    System.out.println("4. View Plane Maintenance History");
                } else if (userRole.equals("Technician")) {
                    System.out.println("1. View Open Maintenance Requests");
                    System.out.println("2. Log a Completed Repair");
                    System.out.println("3. View Repair History for a Plane");
                }

                System.out.println("--------------------------------------");
                System.out.println("9. Log Out");
                System.out.println("======================================");

                int choice = readChoice();
                if (userRole.equals("Customer")) {
                    switch (choice) {
                        case 1: feature1(esql); break;
                        case 2: feature2(esql); break;
                        case 3: feature3(esql); break;
                        case 4: feature4(esql); break;
                        case 5: feature5(esql, authorisedUser); break; // CORRECTED
                        case 6: SearchAvailableFlights(esql); break;
                        case 7: BookFlight(esql, authorisedUser); break; // CORRECTED
                        case 8: ViewMyReservations(esql, authorisedUser); break; // CORRECTED
                        case 9: authorisedUser = null; System.out.println("Logged out successfully."); break; // CORRECTED
                        default: System.out.println("Unrecognized choice!"); break;
                    }
                } else if (userRole.equals("Pilot")) {
                    switch (choice) {
                        case 1: ViewPilotAssignedFlights(esql, authorisedUser); break; // CORRECTED
                        case 2: ViewFlightManifest(esql, authorisedUser); break; // CORRECTED
                        case 3: SubmitMaintenanceRequest(esql, authorisedUser); break; // CORRECTED
                        case 4: ViewPlaneMaintenanceHistoryForPilot(esql); break;
                        case 9: authorisedUser = null; System.out.println("Logged out successfully."); break; // CORRECTED
                        default: System.out.println("Unrecognized choice!"); break;
                    }
                } else if (userRole.equals("Technician")) {
                    switch (choice) {
                        case 1: ViewOpenMaintenanceRequests(esql); break;
                        case 2: LogCompletedRepair(esql, authorisedUser); break; // CORRECTED
                        case 3: ViewPlaneRepairHistoryForTechnician(esql); break;
                        case 9: authorisedUser = null; System.out.println("Logged out successfully."); break; // CORRECTED
                        default: System.out.println("Unrecognized choice!"); break;
                    }
                }
                // This check was slightly redundant if authorisedUser is set to null in the switch cases,
                // but harmless.
                if (choice == 9 && authorisedUser == null) { // CORRECTED
                    // authorisedUser is already null, ready for login menu
                }
            }
        }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user
    **/
   public static void CreateUser(AirlineManagement esql){
    try {
         System.out.println("========== Create New User Account ==========");
         System.out.print("Enter First Name: ");
         String firstName = in.readLine().trim();
         System.out.print("Enter Last Name: ");
         String lastName = in.readLine().trim();
         System.out.print("Enter desired Password: ");
         String password = in.readLine().trim();

         System.out.print("Select User Role (C for Customer, P for Pilot, T for Technician): ");
         String roleChoice = in.readLine().trim().toUpperCase();

         String fullName = firstName + " " + lastName;
         String safeFirstName = firstName.replace("'", "''");
         String safeLastName = lastName.replace("'", "''");
         String safePassword = password.replace("'", "''");
         String safeFullName = fullName.replace("'", "''");


         if (roleChoice.equals("C")) {
            System.out.println("--- Creating Customer Account ---");
            System.out.print("Enter Gender (M/F/Other): ");
            String gender = in.readLine().trim();
            System.out.print("Enter Date of Birth (YYYY-MM-DD): ");
            String dob = in.readLine().trim();
            System.out.print("Enter Address: ");
            String address = in.readLine().trim();
            System.out.print("Enter Phone Number: ");
            String phone = in.readLine().trim();
            System.out.print("Enter Zip Code: ");
            String zip = in.readLine().trim();

            String safeGender = gender.replace("'", "''");
            String safeAddress = address.replace("'", "''");
            String safePhone = phone.replace("'", "''");
            String safeZip = zip.replace("'", "''");

            String queryMaxId = "SELECT MAX(CustomerID) FROM Customer";
            List<List<String>> result = esql.executeQueryAndReturnResult(queryMaxId);
            int nextCustomerId = 1;
            if (result != null && !result.isEmpty() && result.get(0).get(0) != null) {
               try {
                  nextCustomerId = Integer.parseInt(result.get(0).get(0)) + 1;
               } catch (NumberFormatException e) {
                  System.err.println("Warning: Could not parse max CustomerID, defaulting to 1. " + e.getMessage());
               }
            }

            String insertQuery = String.format(
                  "INSERT INTO Customer (CustomerID, FirstName, LastName, Password, Gender, DOB, Address, Phone, Zip) " +
                  "VALUES (%d, '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')",
                  nextCustomerId, safeFirstName, safeLastName, safePassword, safeGender, dob, safeAddress, safePhone, safeZip);
            
            esql.executeUpdate(insertQuery);
            System.out.println("Customer account created successfully for " + fullName + " with CustomerID: " + nextCustomerId);

         // Inside CreateUser, for Pilot:
         } else if (roleChoice.equals("P")) {
            System.out.println("--- Creating Pilot Account ---");
            String prefix = "P";
            String maxIdQuery = "SELECT MAX(CAST(SUBSTRING(PilotID FROM 2) AS INTEGER)) FROM Pilot";
            List<List<String>> result = esql.executeQueryAndReturnResult(maxIdQuery);
            int nextNumericId = 1;
            
            if (result != null && !result.isEmpty() && result.get(0).get(0) != null) {
                nextNumericId = Integer.parseInt(result.get(0).get(0)) + 1;
            }
            
            String pilotId = prefix + String.format("%03d", nextNumericId);
            
            String insertQuery = String.format(
                  "INSERT INTO Pilot (PilotID, Name, Password) VALUES ('%s', '%s', '%s')",
                  pilotId, safeFullName, safePassword);
            
            try {
               esql.executeUpdate(insertQuery);
               System.out.println("Pilot account created successfully for " + fullName + " with PilotID: " + pilotId);
            } catch (SQLException insertEx) {
               System.err.println("Database error during pilot insertion: " + insertEx.getMessage());
               return;
            }
            
         } else if (roleChoice.equals("T")) {
            System.out.println("--- Creating Technician Account ---");
            String maxIdQuery = "SELECT MAX(CAST(SUBSTRING(TechnicianID FROM 2) AS INTEGER)) FROM Technician";
            List<List<String>> result = esql.executeQueryAndReturnResult(maxIdQuery);
            int nextNumericId = 1;
            
            if (result != null && !result.isEmpty() && result.get(0).get(0) != null) {
                nextNumericId = Integer.parseInt(result.get(0).get(0)) + 1;
            }
            
            String technicianId = "T" + String.format("%03d", nextNumericId);
            
            String insertQuery = String.format(
                  "INSERT INTO Technician (TechnicianID, Name, Password) VALUES ('%s', '%s', '%s')",
                  technicianId, safeFullName, safePassword);
            
            try {
               esql.executeUpdate(insertQuery);
               System.out.println("Technician account created successfully for " + fullName + " with TechnicianID: " + technicianId);
            } catch (SQLException insertEx) {
               System.err.println("Database error during technician insertion: " + insertEx.getMessage());
               return;
            }
         } else {
            System.out.println("Invalid role selected. User account not created.");
         }
         System.out.println("============================================");

      } catch (IOException e) {
         System.err.println("Error reading input: " + e.getMessage());
      } catch (SQLException e) {
         System.err.println("Database error during user creation: " + e.getMessage());
      } catch (Exception e) { 
         System.err.println("An unexpected error occurred during user creation: " + e.getMessage());
      }
   }//end CreateUser


   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(AirlineManagement esql){
      String authorisedUser = null;
      String userNameToDisplay = ""; 

      try {
         System.out.println("============== User Login ==============");
         System.out.print("Enter your role (Customer/Pilot/Technician): ");
         String role = in.readLine().trim().toLowerCase(); 
         System.out.print("Enter your ID: ");
         String idStr = in.readLine().trim();
         System.out.print("Enter your password: ");
         String password = in.readLine().trim();

         String query = "";
         List<List<String>> result = null;
         
         String safeIdStr = idStr.replace("'", "''");
         String safePassword = password.replace("'", "''");

         if (role.equals("customer")) {
            try {
                Integer.parseInt(idStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid Customer ID format. Customer ID must be a number.");
                return null;
            }
            query = String.format("SELECT CustomerID, FirstName, LastName FROM Customer WHERE CustomerID = %s AND Password = '%s'", safeIdStr, safePassword);
            result = esql.executeQueryAndReturnResult(query);
            if (result != null && !result.isEmpty()) {
               authorisedUser = "CUSTOMER_" + result.get(0).get(0); // CustomerID
               userNameToDisplay = result.get(0).get(1) + " " + result.get(0).get(2); // FirstName LastName
            }

         } else if (role.equals("pilot")) {
            query = String.format("SELECT PilotID, Name FROM Pilot WHERE PilotID = '%s' AND Password = '%s'", safeIdStr, safePassword);
            result = esql.executeQueryAndReturnResult(query);
            if (result != null && !result.isEmpty()) {
               authorisedUser = "PILOT_" + result.get(0).get(0); // PilotID
               userNameToDisplay = result.get(0).get(1); // Name
            }

         } else if (role.equals("technician")) {
            query = String.format("SELECT TechnicianID, Name FROM Technician WHERE TechnicianID = '%s' AND Password = '%s'", safeIdStr, safePassword);
            result = esql.executeQueryAndReturnResult(query);
            if (result != null && !result.isEmpty()) {
               authorisedUser = "TECHNICIAN_" + result.get(0).get(0); // TechnicianID
               userNameToDisplay = result.get(0).get(1); // Name
            }
         } else {
            System.out.println("Invalid role entered. Please choose Customer, Pilot, or Technician.");
            return null;
         }

         if (authorisedUser != null) {
            System.out.println("\nWelcome, " + userNameToDisplay + "! (" + authorisedUser + ") You are logged in.\n");
         } else {
            System.out.println("\nLogin failed. Invalid ID or password for the specified role.\n");
         }
         System.out.println("======================================");

      } catch (IOException e) {
         System.err.println("Error reading input: " + e.getMessage());
      } catch (SQLException e) {
         System.err.println("Database error during login: " + e.getMessage());
      } catch (Exception e) { 
         System.err.println("An unexpected error occurred during login: " + e.getMessage());
      }
      return authorisedUser;
   }//end

// Rest of the functions definition go in here

   public static void feature1(AirlineManagement esql) {
      try {
        System.out.println("========== View Flights by Origin and Destination ==========");
        System.out.print("Enter Departure City: ");
        String departureCity = in.readLine().trim();
        System.out.print("Enter Arrival City: ");
        String arrivalCity = in.readLine().trim();

        if (departureCity.isEmpty() || arrivalCity.isEmpty()) {
            System.out.println("Departure and Arrival cities cannot be empty.");
            System.out.println("========================================================");
            return;
        }

        String safeDepartureCity = departureCity.replace("'", "''");
        String safeArrivalCity = arrivalCity.replace("'", "''");

        String query = String.format(
            "SELECT F.FlightNumber, F.PlaneID, S.DayOfWeek, S.DepartureTime, S.ArrivalTime " +
            "FROM Flight F INNER JOIN Schedule S ON F.FlightNumber = S.FlightNumber " +
            "WHERE F.DepartureCity = '%s' AND F.ArrivalCity = '%s' " +
            "ORDER BY F.FlightNumber, S.DayOfWeek, S.DepartureTime",
            safeDepartureCity, safeArrivalCity
        );
        System.out.println("\n--- Available Flights and Schedules ---");
        int rowCount = esql.executeQueryAndPrintResult(query);

        if (rowCount == 0) {
            System.out.println("No flights found for the specified route: " + departureCity + " to " + arrivalCity);
        }
        System.out.println("========================================================");

    } catch (IOException e) {
        System.err.println("Error reading input: " + e.getMessage());
    } catch (SQLException e) {
        System.err.println("Database query error: " + e.getMessage());
    } catch (Exception e) {
        System.err.println("An unexpected error occurred: " + e.getMessage());
    }
   }

   public static void feature2(AirlineManagement esql) {
      try {
        System.out.println("========== View Flight Seat Availability ==========");
        System.out.print("Enter Flight Number (e.g., F100): ");
        String flightNumber = in.readLine().trim().toUpperCase(); 
        if (flightNumber.isEmpty()) {
            System.out.println("Flight Number cannot be empty.");
            System.out.println("==================================================");
            return;
        }

        System.out.print("Enter Flight Date (YYYY-MM-DD, leave blank for all dates): ");
        String flightDateStr = in.readLine().trim();
        String safeFlightNumber = flightNumber.replace("'", "''");
        
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT FlightInstanceID, FlightNumber, FlightDate, SeatsTotal, SeatsSold, (SeatsTotal - SeatsSold) AS SeatsAvailable ");
        queryBuilder.append("FROM FlightInstance ");
        queryBuilder.append(String.format("WHERE FlightNumber = '%s' ", safeFlightNumber));

        if (!flightDateStr.isEmpty()) {
            if (!flightDateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                System.out.println("Invalid date format. Please use YYYY-MM-DD or leave blank.");
                System.out.println("==================================================");
                return;
            }
            queryBuilder.append(String.format("AND FlightDate = '%s' ", flightDateStr));
        }

        queryBuilder.append("ORDER BY FlightDate, FlightInstanceID");
        String query = queryBuilder.toString();
        System.out.println("\n--- Seat Availability for Flight " + flightNumber + 
                           (flightDateStr.isEmpty() ? "" : " on " + flightDateStr) + " ---");
        int rowCount = esql.executeQueryAndPrintResult(query);

        if (rowCount == 0) {
            System.out.println("No flight instances found for Flight Number: " + flightNumber + 
                               (flightDateStr.isEmpty() ? "." : " on " + flightDateStr + "."));
        }
        System.out.println("==================================================");

    } catch (IOException e) {
        System.err.println("Error reading input: " + e.getMessage());
    } catch (SQLException e) {
        System.err.println("Database query error: " + e.getMessage());
    } catch (Exception e) {
        System.err.println("An unexpected error occurred: " + e.getMessage());
    }
   }
   public static void feature3(AirlineManagement esql) {
      try {
        System.out.println("========== View Flight Departure/Arrival Status ==========");
        System.out.print("Enter Flight Number (e.g., F100): ");
        String flightNumber = in.readLine().trim().toUpperCase();

        if (flightNumber.isEmpty()) {
            System.out.println("Flight Number cannot be empty.");
            System.out.println("========================================================");
            return;
        }

        System.out.print("Enter Flight Date (YYYY-MM-DD, leave blank for all dates): ");
        String flightDateStr = in.readLine().trim();

        String safeFlightNumber = flightNumber.replace("'", "''");
        
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT FlightInstanceID, FlightNumber, FlightDate, DepartedOnTime, ArrivedOnTime ");
        queryBuilder.append("FROM FlightInstance ");
        queryBuilder.append(String.format("WHERE FlightNumber = '%s' ", safeFlightNumber));

        if (!flightDateStr.isEmpty()) {
            if (!flightDateStr.matches("\\d{4}-\\d{2}-\\d{2}")) { // YYYY-MM-DD check
                System.out.println("Invalid date format. Please use YYYY-MM-DD or leave blank.");
                System.out.println("========================================================");
                return;
            }
            queryBuilder.append(String.format("AND FlightDate = '%s' ", flightDateStr));
        }
        queryBuilder.append("ORDER BY FlightDate, FlightInstanceID");
        String query = queryBuilder.toString();
        System.out.println("\n--- Status for Flight " + flightNumber + 
                           (flightDateStr.isEmpty() ? "" : " on " + flightDateStr) + " ---");
        int rowCount = esql.executeQueryAndPrintResult(query);

        if (rowCount == 0) {
            System.out.println("No flight instances found for Flight Number: " + flightNumber + 
                               (flightDateStr.isEmpty() ? "." : " on " + flightDateStr + "."));
        }
        System.out.println("========================================================");

    } catch (IOException e) {
        System.err.println("Error reading input: " + e.getMessage());
    } catch (SQLException e) {
        System.err.println("Database query error: " + e.getMessage());
    } catch (Exception e) {
        System.err.println("An unexpected error occurred: " + e.getMessage());
    }
   }

   public static void feature4(AirlineManagement esql) {
      try {
        System.out.println("========== View All Flights for a Specific Date ==========");
        System.out.print("Enter Flight Date (YYYY-MM-DD): ");
        String flightDateStr = in.readLine().trim();

        if (flightDateStr.isEmpty()) {
            System.out.println("Flight Date cannot be empty.");
            System.out.println("=======================================================");
            return;
        }
        
        if (!flightDateStr.matches("\\d{4}-\\d{2}-\\d{2}")) { // YYYY-MM-DD check
            System.out.println("Invalid date format. Please use YYYY-MM-DD.");
            System.out.println("=======================================================");
            return;
        }

        String query = String.format(
            "SELECT FI.FlightInstanceID, FI.FlightNumber, F.DepartureCity, F.ArrivalCity, FI.FlightDate, " +
            "S.DepartureTime, S.ArrivalTime, FI.DepartedOnTime, FI.ArrivedOnTime " +
            "FROM FlightInstance FI " +
            "INNER JOIN Flight F ON FI.FlightNumber = F.FlightNumber " +
            "INNER JOIN Schedule S ON FI.FlightNumber = S.FlightNumber " +
            "WHERE FI.FlightDate = '%s' AND TRIM(TO_CHAR(FI.FlightDate, 'Day')) = S.DayOfWeek " +
            "ORDER BY S.DepartureTime, FI.FlightNumber",
            flightDateStr
        );
        System.out.println("\n--- Flights Scheduled for " + flightDateStr + " ---");
        int rowCount = esql.executeQueryAndPrintResult(query);

        if (rowCount == 0) {
            System.out.println("No flights found scheduled for " + flightDateStr + ".");
        }
        System.out.println("=======================================================");

    } catch (IOException e) {
        System.err.println("Error reading input: " + e.getMessage());
    } catch (SQLException e) {
        System.err.println("Database query error: " + e.getMessage());
    } catch (Exception e) {
        System.err.println("An unexpected error occurred: " + e.getMessage());
    }
   }

   public static void feature5(AirlineManagement esql, String authorisedUser) { // View User's Order ID History
    try {
        System.out.println("========== Your Reservation History ==========");

        if (authorisedUser == null || !authorisedUser.startsWith("CUSTOMER_")) {
            System.out.println("You must be logged in as a Customer to view your order history.");
            System.out.println("Or, this feature is intended for customers only.");
             System.out.println("==================================================");
            return;
        }
        String customerIdStr = "";
        try {
            customerIdStr = authorisedUser.substring("CUSTOMER_".length());
            Integer.parseInt(customerIdStr); // Validate it's a number
        } catch (Exception e) {
            System.err.println("Invalid authorised user format for customer ID extraction: " + authorisedUser);
            System.out.println("==================================================");
            return;
        }
        
        String query = String.format(
            "SELECT R.ReservationID, R.Status, " +
            "FI.FlightNumber, F.DepartureCity, F.ArrivalCity, FI.FlightDate, " +
            "FI.TicketCost " +
            "FROM Reservation R " +
            "INNER JOIN FlightInstance FI ON R.FlightInstanceID = FI.FlightInstanceID " +
            "INNER JOIN Flight F ON FI.FlightNumber = F.FlightNumber " +
            "WHERE R.CustomerID = %s " + 
            "ORDER BY FI.FlightDate DESC, R.ReservationID",
            customerIdStr
        );
        System.out.println("\n--- Your Reservations ---");
        int rowCount = esql.executeQueryAndPrintResult(query);

        if (rowCount == 0) {
            System.out.println("You have no reservations in the system.");
        }
        System.out.println("==================================================");

    } catch (SQLException e) {
        System.err.println("Database query error: " + e.getMessage());
    } catch (Exception e) {
        System.err.println("An unexpected error occurred: " + e.getMessage());
    }
   }

   public static void feature6(AirlineManagement esql) {}
  
   public static void SearchAvailableFlights(AirlineManagement esql) {
      System.out.println("========== Search Available Flights ==========");
      try {
         System.out.print("Enter Departure City: ");
         String departureCity = in.readLine().trim();
         System.out.print("Enter Arrival City: ");
         String arrivalCity = in.readLine().trim();
         System.out.print("Enter Travel Date (YYYY-MM-DD): ");
         String travelDateStr = in.readLine().trim();
         System.out.print("Enter Number of Passengers (e.g., 1): ");
         String numPassengersStr = in.readLine().trim();
         int numPassengers = 1; // Default to 1 passenger

         if (departureCity.isEmpty() || arrivalCity.isEmpty() || travelDateStr.isEmpty()) {
               System.out.println("Departure city, arrival city, and travel date cannot be empty.");
               System.out.println("==============================================");
               return;
         }

         if (!travelDateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
               System.out.println("Invalid travel date format. Please use YYYY-MM-DD.");
               System.out.println("==============================================");
               return;
         }

         try {
               if (!numPassengersStr.isEmpty()) {
                  numPassengers = Integer.parseInt(numPassengersStr);
                  if (numPassengers <= 0) {
                     System.out.println("Number of passengers must be a positive number.");
                     System.out.println("==============================================");
                     return;
                  }
               }
         } catch (NumberFormatException e) {
               System.out.println("Invalid number of passengers. Please enter a number.");
               System.out.println("==============================================");
               return;
         }

         String safeDepartureCity = departureCity.replace("'", "''");
         String safeArrivalCity = arrivalCity.replace("'", "''");

         // Query to find flight instances matching criteria with available seats
         // Joining with Schedule to get the scheduled times for that day of week.
         String query = String.format(
               "SELECT FI.FlightInstanceID, FI.FlightNumber, F.DepartureCity, F.ArrivalCity, FI.FlightDate, " +
               "S.DepartureTime, S.ArrivalTime, FI.TicketCost, (FI.SeatsTotal - FI.SeatsSold) AS SeatsAvailable " +
               "FROM FlightInstance FI " +
               "INNER JOIN Flight F ON FI.FlightNumber = F.FlightNumber " +
               "INNER JOIN Schedule S ON F.FlightNumber = S.FlightNumber AND TRIM(TO_CHAR(FI.FlightDate, 'Day')) = S.DayOfWeek " +
               "WHERE F.DepartureCity = '%s' " +
               "AND F.ArrivalCity = '%s' " +
               "AND FI.FlightDate = '%s' " +
               "AND (FI.SeatsTotal - FI.SeatsSold) >= %d " + // Check for available seats
               "ORDER BY S.DepartureTime",
               safeDepartureCity, safeArrivalCity, travelDateStr, numPassengers
         );

         /* // --- PreparedStatement Example ---
         String queryPS = "SELECT FI.FlightInstanceID, FI.FlightNumber, F.DepartureCity, F.ArrivalCity, FI.FlightDate, " +
                           "S.DepartureTime, S.ArrivalTime, FI.TicketCost, (FI.SeatsTotal - FI.SeatsSold) AS SeatsAvailable " +
                           "FROM FlightInstance FI " +
                           "INNER JOIN Flight F ON FI.FlightNumber = F.FlightNumber " +
                           "INNER JOIN Schedule S ON F.FlightNumber = S.FlightNumber AND TRIM(TO_CHAR(FI.FlightDate, 'Day')) = S.DayOfWeek " +
                           "WHERE F.DepartureCity = ? AND F.ArrivalCity = ? AND FI.FlightDate = ? AND (FI.SeatsTotal - FI.SeatsSold) >= ? " +
                           "ORDER BY S.DepartureTime";
         PreparedStatement pstmt = esql.getConnection().prepareStatement(queryPS);
         pstmt.setString(1, departureCity);
         pstmt.setString(2, arrivalCity);
         pstmt.setDate(3, java.sql.Date.valueOf(travelDateStr));
         pstmt.setInt(4, numPassengers);
         // ... execute and process ResultSet ...
         */
         
         System.out.println("\n--- Available Flights Matching Your Search ---");
         int rowCount = esql.executeQueryAndPrintResult(query);

         if (rowCount == 0) {
               System.out.println("No available flights found for your criteria.");
         } else {
               System.out.println("You can use the FlightInstanceID to book a flight.");
         }
         System.out.println("==============================================");

      } catch (IOException e) {
         System.err.println("Error reading input: " + e.getMessage());
      } catch (SQLException e) {
         System.err.println("Database query error: " + e.getMessage());
      } catch (Exception e) {
         System.err.println("An unexpected error occurred: " + e.getMessage());
      }
   }
   public static void BookFlight(AirlineManagement esql, String authorisedUser) {
    System.out.println("========== Book a Flight ==========");
    if (authorisedUser == null || !authorisedUser.startsWith("CUSTOMER_")) {
        System.out.println("You must be logged in as a Customer to book flights.");
        System.out.println("=================================");
        return;
    }

    String customerIdStr = authorisedUser.substring("CUSTOMER_".length());
    int customerId;
    try {
        customerId = Integer.parseInt(customerIdStr);
    } catch (NumberFormatException e) {
        System.err.println("Invalid customer ID format in authorisedUser string.");
        System.out.println("=================================");
        return;
    }

    try {
        System.out.print("Enter the FlightInstanceID you wish to book (from search results): ");
        String flightInstanceIdStr = in.readLine().trim();
        int flightInstanceId;
        try {
            flightInstanceId = Integer.parseInt(flightInstanceIdStr);
        } catch (NumberFormatException e) {
            System.out.println("Invalid FlightInstanceID format. Please enter a number.");
            System.out.println("=================================");
            return;
        }

        // --- Start Transaction ---
        esql._connection.setAutoCommit(false); // Manually manage transaction

        try {
            // 1. Check seat availability and get current SeatsSold
            String checkSeatsQuery = String.format(
                "SELECT SeatsTotal, SeatsSold FROM FlightInstance WHERE FlightInstanceID = %d FOR UPDATE", // FOR UPDATE locks the row
                flightInstanceId
            );
            List<List<String>> seatInfo = esql.executeQueryAndReturnResult(checkSeatsQuery);

            if (seatInfo == null || seatInfo.isEmpty()) {
                System.out.println("FlightInstanceID " + flightInstanceId + " not found.");
                esql._connection.rollback(); // Rollback transaction
                System.out.println("=================================");
                return;
            }

            int seatsTotal = Integer.parseInt(seatInfo.get(0).get(0));
            int seatsSold = Integer.parseInt(seatInfo.get(0).get(1));
            String reservationStatus;

            if ((seatsTotal - seatsSold) > 0) { // Seats available
                reservationStatus = "reserved";
                // Increment SeatsSold
                String updateSeatsQuery = String.format(
                    "UPDATE FlightInstance SET SeatsSold = SeatsSold + 1 WHERE FlightInstanceID = %d",
                    flightInstanceId
                );
                esql.executeUpdate(updateSeatsQuery);
                System.out.println("Seat count updated for FlightInstanceID: " + flightInstanceId);
            } else { // No seats available, offer waitlist
                System.out.println("This flight is currently full.");
                System.out.print("Would you like to be added to the waitlist? (yes/no): ");
                String waitlistChoice = in.readLine().trim().toLowerCase();
                if (waitlistChoice.equals("yes")) {
                    reservationStatus = "waitlist";
                } else {
                    System.out.println("Booking cancelled.");
                    esql._connection.rollback(); // Rollback transaction
                    System.out.println("=================================");
                    return;
                }
            }

            // 2. Generate new ReservationID (simple MAX+1, vulnerable to race conditions without proper DB sequences)
            // Example: R0001, R0002. This needs a more robust ID generation in a real system.
            // For now, let's use a simplified numeric part.
            String queryMaxResId = "SELECT MAX(CAST(SUBSTRING(ReservationID FROM 2) AS INTEGER)) FROM Reservation WHERE ReservationID LIKE 'R%'";
            List<List<String>> resIdResult = esql.executeQueryAndReturnResult(queryMaxResId);
            int nextResNumericId = 1;
            if (resIdResult != null && !resIdResult.isEmpty() && resIdResult.get(0).get(0) != null) {
                try {
                    nextResNumericId = Integer.parseInt(resIdResult.get(0).get(0)) + 1;
                } catch (NumberFormatException e) { /* use default */ }
            }
            String reservationId = "R" + String.format("%04d", nextResNumericId);

            // 3. Insert into Reservation table
            String insertReservationQuery = String.format(
                "INSERT INTO Reservation (ReservationID, CustomerID, FlightInstanceID, Status) " +
                "VALUES ('%s', %d, %d, '%s')",
                reservationId, customerId, flightInstanceId, reservationStatus
            );
            esql.executeUpdate(insertReservationQuery);

            esql._connection.commit(); // Commit transaction
            System.out.println("Reservation " + reservationId + " created with status: " + reservationStatus);

        } catch (SQLException e) {
            esql._connection.rollback(); // Rollback on any SQL error during transaction
            System.err.println("Database error during booking transaction: " + e.getMessage());
            // e.printStackTrace();
        } catch (NumberFormatException e) {
            esql._connection.rollback();
            System.err.println("Error parsing seat information or IDs.");
        } finally {
            esql._connection.setAutoCommit(true); // Always reset auto-commit
        }
        System.out.println("=================================");

    } catch (IOException e) {
        System.err.println("Error reading input: " + e.getMessage());
    } catch (SQLException e) { // For the setAutoCommit calls outside the transaction block
        System.err.println("Database error setting auto-commit: " + e.getMessage());
    } catch (Exception e) {
        System.err.println("An unexpected error occurred: " + e.getMessage());
    }
}

// Customer Feature
public static void ViewMyReservations(AirlineManagement esql, String authorisedUser) {
    System.out.println("========== My Current Reservations ==========");
    if (authorisedUser == null || !authorisedUser.startsWith("CUSTOMER_")) {
        System.out.println("You must be logged in as a Customer to view your reservations.");
        System.out.println("============================================");
        return;
    }

    String customerIdStr = "";
    int customerId;
    try {
        customerIdStr = authorisedUser.substring("CUSTOMER_".length());
        customerId = Integer.parseInt(customerIdStr);
    } catch (Exception e) {
        System.err.println("Invalid authorised user format: " + authorisedUser);
        System.out.println("============================================");
        return;
    }

    try {
        String query = String.format(
            "SELECT R.ReservationID, R.Status, FI.FlightNumber, F.DepartureCity, F.ArrivalCity, FI.FlightDate, " +
            "S.DepartureTime, S.ArrivalTime, FI.TicketCost " +
            "FROM Reservation R " +
            "INNER JOIN FlightInstance FI ON R.FlightInstanceID = FI.FlightInstanceID " +
            "INNER JOIN Flight F ON FI.FlightNumber = F.FlightNumber " +
            "INNER JOIN Schedule S ON F.FlightNumber = S.FlightNumber AND TRIM(TO_CHAR(FI.FlightDate, 'Day')) = S.DayOfWeek " +
            "WHERE R.CustomerID = %d AND R.Status IN ('reserved', 'waitlist') " +
            "ORDER BY FI.FlightDate, S.DepartureTime",
            customerId
        );
        
        /* // --- PreparedStatement Example ---
        String queryPS = "SELECT R.ReservationID, R.Status, FI.FlightNumber, F.DepartureCity, F.ArrivalCity, FI.FlightDate, " +
                         "S.DepartureTime, S.ArrivalTime, FI.TicketCost " +
                         "FROM Reservation R " +
                         "INNER JOIN FlightInstance FI ON R.FlightInstanceID = FI.FlightInstanceID " +
                         "INNER JOIN Flight F ON FI.FlightNumber = F.FlightNumber " +
                         "INNER JOIN Schedule S ON F.FlightNumber = S.FlightNumber AND TRIM(TO_CHAR(FI.FlightDate, 'Day')) = S.DayOfWeek " +
                         "WHERE R.CustomerID = ? AND R.Status IN ('reserved', 'waitlist') " +
                         "ORDER BY FI.FlightDate, S.DepartureTime";
        PreparedStatement pstmt = esql.getConnection().prepareStatement(queryPS);
        pstmt.setInt(1, customerId);
        // ... execute and process ResultSet ...
        */

        System.out.println("\n--- Your Active and Waitlisted Reservations ---");
        int rowCount = esql.executeQueryAndPrintResult(query);

        if (rowCount == 0) {
            System.out.println("You have no current (reserved or waitlisted) reservations.");
        }
        System.out.println("============================================");

    } catch (SQLException e) {
        System.err.println("Database query error: " + e.getMessage());
    } catch (Exception e) {
        System.err.println("An unexpected error occurred: " + e.getMessage());
    }
}

// New Pilot Functions
public static void ViewPilotAssignedFlights(AirlineManagement esql, String authorisedUser) {
    System.out.println("========== View Flights for a Specific Date (Pilot View) ==========");
    if (authorisedUser == null || !authorisedUser.startsWith("PILOT_")) {
        System.out.println("Access Denied. This feature is for pilots.");
        System.out.println("==================================================================");
        return;
    }
    // String pilotId = authorisedUser.substring("PILOT_".length()); // Not used directly in this version

    try {
        System.out.print("Enter Flight Date to view schedule (YYYY-MM-DD): ");
        String flightDateStr = in.readLine().trim();

        if (flightDateStr.isEmpty()) {
            System.out.println("Flight Date cannot be empty.");
            System.out.println("==================================================================");
            return;
        }
        
        if (!flightDateStr.matches("\\d{4}-\\d{2}-\\d{2}")) { // Basic YYYY-MM-DD check
            System.out.println("Invalid date format. Please use YYYY-MM-DD.");
            System.out.println("==================================================================");
            return;
        }

        // This query is similar to feature4, showing all operational flights on a given day.
        // The current schema does not have a direct Pilot-to-FlightInstance assignment table.
        String query = String.format(
            "SELECT FI.FlightInstanceID, FI.FlightNumber, F.PlaneID, F.DepartureCity, F.ArrivalCity, FI.FlightDate, " +
            "S.DepartureTime, S.ArrivalTime, FI.DepartedOnTime, FI.ArrivedOnTime " +
            "FROM FlightInstance FI " +
            "INNER JOIN Flight F ON FI.FlightNumber = F.FlightNumber " +
            "INNER JOIN Schedule S ON FI.FlightNumber = S.FlightNumber AND TRIM(TO_CHAR(FI.FlightDate, 'Day')) = S.DayOfWeek " +
            "WHERE FI.FlightDate = '%s' " +
            "ORDER BY S.DepartureTime, FI.FlightNumber",
            flightDateStr
        );
        
        System.out.println("\n--- All Flights Scheduled for " + flightDateStr + " ---");
        System.out.println("(Note: This view shows all operational flights. Specific pilot assignments are not tracked in the current system.)");
        int rowCount = esql.executeQueryAndPrintResult(query);

        if (rowCount == 0) {
            System.out.println("No flights found scheduled for " + flightDateStr + ".");
        }
        System.out.println("==================================================================");

    } catch (IOException e) {
        System.err.println("Error reading input: " + e.getMessage());
    } catch (SQLException e) {
        System.err.println("Database query error: " + e.getMessage());
    } catch (Exception e) {
        System.err.println("An unexpected error occurred: " + e.getMessage());
    }
}

// Pilot Feature
public static void ViewFlightManifest(AirlineManagement esql, String authorisedUser) {
    System.out.println("========== View Flight Manifest (Passenger List) ==========");
     if (authorisedUser == null || !authorisedUser.startsWith("PILOT_")) {
        System.out.println("Access Denied. This feature is for pilots.");
        System.out.println("==========================================================");
        return;
    }

    try {
        System.out.print("Enter FlightInstanceID to view its manifest: ");
        String flightInstanceIdStr = in.readLine().trim();
        int flightInstanceId;

        try {
            flightInstanceId = Integer.parseInt(flightInstanceIdStr);
        } catch (NumberFormatException e) {
            System.out.println("Invalid FlightInstanceID format. Please enter a number.");
            System.out.println("==========================================================");
            return;
        }

        // Query to get passenger list for the given FlightInstanceID
        // Shows customers who are 'reserved' or have 'flown' (in case manifest is checked post-flight)
        String query = String.format(
            "SELECT C.CustomerID, C.FirstName, C.LastName, R.Status " +
            "FROM Reservation R " +
            "INNER JOIN Customer C ON R.CustomerID = C.CustomerID " +
            "WHERE R.FlightInstanceID = %d AND R.Status IN ('reserved', 'flown') " +
            "ORDER BY C.LastName, C.FirstName",
            flightInstanceId
        );

        /* // --- PreparedStatement Example ---
        String queryPS = "SELECT C.CustomerID, C.FirstName, C.LastName, R.Status " +
                         "FROM Reservation R INNER JOIN Customer C ON R.CustomerID = C.CustomerID " +
                         "WHERE R.FlightInstanceID = ? AND R.Status IN ('reserved', 'flown') " +
                         "ORDER BY C.LastName, C.FirstName";
        PreparedStatement pstmt = esql.getConnection().prepareStatement(queryPS);
        pstmt.setInt(1, flightInstanceId);
        // ... execute and process ResultSet ...
        */

        System.out.println("\n--- Passenger Manifest for FlightInstanceID: " + flightInstanceId + " ---");
        int rowCount = esql.executeQueryAndPrintResult(query);

        if (rowCount == 0) {
            System.out.println("No passengers (reserved or flown) found for this flight instance, or FlightInstanceID is invalid.");
        }
        System.out.println("==========================================================");

    } catch (IOException e) {
        System.err.println("Error reading input: " + e.getMessage());
    } catch (SQLException e) {
        System.err.println("Database query error: " + e.getMessage());
    } catch (Exception e) {
        System.err.println("An unexpected error occurred: " + e.getMessage());
    }
}

// Pilot Feature
public static void SubmitMaintenanceRequest(AirlineManagement esql, String authorisedUser) {
    System.out.println("========== Submit Maintenance Request ==========");
    if (authorisedUser == null || !authorisedUser.startsWith("PILOT_")) {
        System.out.println("Access Denied. Only pilots can submit maintenance requests.");
        System.out.println("============================================");
        return;
    }
    String pilotId = authorisedUser.substring("PILOT_".length());

    try {
        System.out.println("\n--- Available Planes ---");
        esql.executeQueryAndPrintResult("SELECT PlaneID, Make, Model FROM Plane ORDER BY PlaneID");
        System.out.print("\nEnter Plane ID for the maintenance request (e.g., PL001): ");
        String planeId = in.readLine().trim().toUpperCase();

        // Simple list of repair codes, can be expanded or read from a table if desired
        System.out.println("\n--- Common Repair Codes ---");
        System.out.println("RC001 - Engine Check");
        System.out.println("RC002 - Landing Gear Issue");
        System.out.println("RC003 - Navigation System Fault");
        System.out.println("RC004 - Cabin Pressurization");
        System.out.println("RC005 - Other (Specify in notes if system allowed)");
        System.out.print("Enter Repair Code (e.g., RC001): ");
        String repairCode = in.readLine().trim().toUpperCase();

        System.out.print("Enter Request Date (YYYY-MM-DD, or leave blank for today): ");
        String requestDateStr = in.readLine().trim();
        
        if (planeId.isEmpty() || repairCode.isEmpty()) {
            System.out.println("Plane ID and Repair Code cannot be empty.");
            System.out.println("============================================");
            return;
        }

        String requestDate;
        if (requestDateStr.isEmpty()) {
            requestDate = java.time.LocalDate.now().toString(); // Defaults to today
        } else {
            if (!requestDateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                System.out.println("Invalid date format. Please use YYYY-MM-DD or leave blank.");
                 System.out.println("============================================");
                return;
            }
            requestDate = requestDateStr;
        }

        // Generate new RequestID (MAX + 1)
        String queryMaxId = "SELECT MAX(RequestID) FROM MaintenanceRequest";
        List<List<String>> result = esql.executeQueryAndReturnResult(queryMaxId);
        int nextRequestId = 1;
        if (result != null && !result.isEmpty() && result.get(0).get(0) != null) {
            try {
                nextRequestId = Integer.parseInt(result.get(0).get(0)) + 1;
            } catch (NumberFormatException e) { /* ignore, use default */ }
        }

        String safePlaneId = planeId.replace("'", "''");
        String safeRepairCode = repairCode.replace("'", "''");
        String safePilotId = pilotId.replace("'", "''");

        String insertQuery = String.format(
            "INSERT INTO MaintenanceRequest (RequestID, PlaneID, RepairCode, RequestDate, PilotID) " +
            "VALUES (%d, '%s', '%s', '%s', '%s')",
            nextRequestId, safePlaneId, safeRepairCode, requestDate, safePilotId
        );

        /* // --- PreparedStatement Example ---
        String insertQueryPS = "INSERT INTO MaintenanceRequest (RequestID, PlaneID, RepairCode, RequestDate, PilotID) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement pstmt = esql.getConnection().prepareStatement(insertQueryPS);
        pstmt.setInt(1, nextRequestId);
        pstmt.setString(2, planeId);
        pstmt.setString(3, repairCode);
        pstmt.setDate(4, java.sql.Date.valueOf(requestDate));
        pstmt.setString(5, pilotId);
        pstmt.executeUpdate();
        pstmt.close();
        */
        
        esql.executeUpdate(insertQuery);
        System.out.println("Maintenance Request submitted successfully with RequestID: " + nextRequestId);
        System.out.println("============================================");

    } catch (IOException e) {
        System.err.println("Error reading input: " + e.getMessage());
    } catch (SQLException e) {
        System.err.println("Database error: " + e.getMessage());
    } catch (Exception e) {
        System.err.println("An unexpected error occurred: " + e.getMessage());
    }
}

// Pilot Feature (can also be used by Technicians if menu structure allows)
public static void ViewPlaneMaintenanceHistoryForPilot(AirlineManagement esql) {
    System.out.println("========== View Plane Maintenance & Repair History ==========");
    try {
        System.out.print("Enter Plane ID to view its history (e.g., PL001): ");
        String planeId = in.readLine().trim().toUpperCase();

        if (planeId.isEmpty()) {
            System.out.println("Plane ID cannot be empty.");
            System.out.println("========================================================");
            return;
        }
        String safePlaneId = planeId.replace("'", "''");

        System.out.println("\n--- Maintenance Requests for Plane " + planeId + " ---");
        String requestsQuery = String.format(
            "SELECT MR.RequestID, MR.RepairCode, MR.RequestDate, MR.PilotID, P.Name AS PilotName " +
            "FROM MaintenanceRequest MR INNER JOIN Pilot P ON MR.PilotID = P.PilotID " +
            "WHERE MR.PlaneID = '%s' ORDER BY MR.RequestDate DESC",
            safePlaneId
        );
        int requestCount = esql.executeQueryAndPrintResult(requestsQuery);
        if (requestCount == 0) {
            System.out.println("No maintenance requests found for this plane.");
        }

        System.out.println("\n--- Repair History for Plane " + planeId + " ---");
        String repairsQuery = String.format(
            "SELECT R.RepairID, R.RepairCode, R.RepairDate, R.TechnicianID, T.Name AS TechnicianName " +
            "FROM Repair R INNER JOIN Technician T ON R.TechnicianID = T.TechnicianID " +
            "WHERE R.PlaneID = '%s' ORDER BY R.RepairDate DESC",
            safePlaneId
        );
        int repairCount = esql.executeQueryAndPrintResult(repairsQuery);
        if (repairCount == 0) {
            System.out.println("No repair history found for this plane.");
        }
        System.out.println("========================================================");

    } catch (IOException e) {
        System.err.println("Error reading input: " + e.getMessage());
    } catch (SQLException e) {
        System.err.println("Database query error: " + e.getMessage());
    } catch (Exception e) {
        System.err.println("An unexpected error occurred: " + e.getMessage());
    }
}


// New Technician Functions
public static void ViewOpenMaintenanceRequests(AirlineManagement esql) {
    System.out.println("========== View Open/All Maintenance Requests ==========");
    try {
        String query = "SELECT MR.RequestID, MR.PlaneID, P.Make, P.Model, MR.RepairCode, MR.RequestDate, MR.PilotID, PI.Name AS PilotName " +
                       "FROM MaintenanceRequest MR " +
                       "INNER JOIN Plane P ON MR.PlaneID = P.PlaneID " +
                       "INNER JOIN Pilot PI ON MR.PilotID = PI.PilotID " +
                       "ORDER BY MR.RequestDate DESC, MR.RequestID";
        
        System.out.println("\n--- All Logged Maintenance Requests ---");
        int rowCount = esql.executeQueryAndPrintResult(query);

        if (rowCount == 0) {
            System.out.println("No maintenance requests found in the system.");
        }
        System.out.println("====================================================");

    } catch (SQLException e) {
        System.err.println("Database query error: " + e.getMessage());
    } catch (Exception e) {
        System.err.println("An unexpected error occurred: " + e.getMessage());
    }
}

public static void LogCompletedRepair(AirlineManagement esql, String authorisedUser) {
    System.out.println("========== Log a Completed Repair ==========");
    try {
        if (authorisedUser == null || !authorisedUser.startsWith("TECHNICIAN_")) {
            System.out.println("Access Denied. Only technicians can log repairs.");
            System.out.println("==========================================");
            return;
        }
        String technicianId = authorisedUser.substring("TECHNICIAN_".length());

        System.out.print("Enter Plane ID of the repaired plane (e.g., PL001): ");
        String planeId = in.readLine().trim().toUpperCase();
        System.out.print("Enter Repair Code for the work done (e.g., RC001): ");
        String repairCode = in.readLine().trim().toUpperCase();
        System.out.print("Enter Repair Date (YYYY-MM-DD, or leave blank for today): ");
        String repairDateStr = in.readLine().trim();
        
        // Optional: Link to a MaintenanceRequestID
        System.out.print("Enter original Maintenance Request ID if this repair resolves it (leave blank if not applicable): ");
        String requestIdStr = in.readLine().trim();


        if (planeId.isEmpty() || repairCode.isEmpty()) {
            System.out.println("Plane ID and Repair Code cannot be empty.");
            System.out.println("==========================================");
            return;
        }

        String repairDate;
        if (repairDateStr.isEmpty()) {
            repairDate = java.time.LocalDate.now().toString(); // Defaults to today
        } else {
            if (!repairDateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                System.out.println("Invalid date format. Please use YYYY-MM-DD or leave blank for today.");
                System.out.println("==========================================");
                return;
            }
            repairDate = repairDateStr;
        }
        
        // Generate new RepairID (MAX + 1)
        String queryMaxId = "SELECT MAX(RepairID) FROM Repair";
        List<List<String>> result = esql.executeQueryAndReturnResult(queryMaxId);
        int nextRepairId = 1;
        if (result != null && !result.isEmpty() && result.get(0).get(0) != null) {
            try {
                nextRepairId = Integer.parseInt(result.get(0).get(0)) + 1;
            } catch (NumberFormatException e) { /* ignore, use default */ }
        }

        // Basic escaping
        String safePlaneId = planeId.replace("'", "''");
        String safeRepairCode = repairCode.replace("'", "''");
        String safeTechnicianId = technicianId.replace("'", "''");


        String insertQuery = String.format(
            "INSERT INTO Repair (RepairID, PlaneID, RepairCode, RepairDate, TechnicianID) " +
            "VALUES (%d, '%s', '%s', '%s', '%s')",
            nextRepairId, safePlaneId, safeRepairCode, repairDate, safeTechnicianId
        );
        
        esql.executeUpdate(insertQuery);
        System.out.println("Repair logged successfully with RepairID: " + nextRepairId);

        if (!requestIdStr.isEmpty()) {
            try {
                int reqId = Integer.parseInt(requestIdStr);
                // To truly mark as "closed", you'd need a status column.
                // For now, we could just note that a repair potentially linked to it was made.
                // Or, if MaintenanceRequest had a 'ResolvedByRepairID' FK column:
                // String updateMaintenanceQuery = String.format("UPDATE MaintenanceRequest SET ResolvedByRepairID = %d WHERE RequestID = %d", nextRepairId, reqId);
                // esql.executeUpdate(updateMaintenanceQuery);
                // System.out.println("Maintenance Request " + reqId + " potentially addressed.");
                System.out.println("(Note: To explicitly close Maintenance Request " + reqId + ", schema update for status is recommended.)");
            } catch (NumberFormatException e) {
                System.out.println("Invalid Maintenance Request ID format entered: " + requestIdStr);
            }
        }
        // Also, update Plane.LastRepairDate
        String updatePlaneQuery = String.format("UPDATE Plane SET LastRepairDate = '%s' WHERE PlaneID = '%s'", repairDate, safePlaneId);
        esql.executeUpdate(updatePlaneQuery);
        System.out.println("Plane " + planeId + " LastRepairDate updated to " + repairDate);


        System.out.println("==========================================");

    } catch (IOException e) {
        System.err.println("Error reading input: " + e.getMessage());
    } catch (SQLException e) {
        System.err.println("Database error: " + e.getMessage());
    } catch (Exception e) {
        System.err.println("An unexpected error occurred: " + e.getMessage());
    }
}

public static void ViewPlaneRepairHistoryForTechnician(AirlineManagement esql) {
    System.out.println("========== View Repair History for a Plane ==========");
    try {
        System.out.print("Enter Plane ID to view its repair history (e.g., PL001): ");
        String planeId = in.readLine().trim().toUpperCase();

        if (planeId.isEmpty()) {
            System.out.println("Plane ID cannot be empty.");
             System.out.println("===================================================");
            return;
        }
        String safePlaneId = planeId.replace("'", "''");

        String query = String.format(
            "SELECT R.RepairID, R.RepairCode, R.RepairDate, R.TechnicianID, T.Name AS TechnicianName " +
            "FROM Repair R INNER JOIN Technician T ON R.TechnicianID = T.TechnicianID " +
            "WHERE R.PlaneID = '%s' " +
            "ORDER BY R.RepairDate DESC, R.RepairID DESC",
            safePlaneId
        );
        
        /* // --- PreparedStatement Example ---
        String queryPS = "SELECT R.RepairID, R.RepairCode, R.RepairDate, R.TechnicianID, T.Name AS TechnicianName " +
                         "FROM Repair R INNER JOIN Technician T ON R.TechnicianID = T.TechnicianID " +
                         "WHERE R.PlaneID = ? ORDER BY R.RepairDate DESC, R.RepairID DESC";
        PreparedStatement pstmt = esql.getConnection().prepareStatement(queryPS);
        pstmt.setString(1, planeId);
        // ... execute and process ResultSet ...
        */

        System.out.println("\n--- Repair History for Plane " + planeId + " ---");
        int rowCount = esql.executeQueryAndPrintResult(query);

        if (rowCount == 0) {
            System.out.println("No repair history found for Plane ID: " + planeId);
        }
        System.out.println("===================================================");

    } catch (IOException e) {
        System.err.println("Error reading input: " + e.getMessage());
    } catch (SQLException e) {
        System.err.println("Database query error: " + e.getMessage());
    } catch (Exception e) {
        System.err.println("An unexpected error occurred: " + e.getMessage());
    }
}
}//end AirlineManagement

