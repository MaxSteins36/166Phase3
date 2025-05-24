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

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");

                //**the following functionalities should only be able to be used by Management**
                System.out.println("1. View Flights");
                System.out.println("2. View Flight Seats");
                System.out.println("3. View Flight Status");
                System.out.println("4. View Flights of the day");  
                System.out.println("5. View Full Order ID History");
                System.out.println(".........................");
                System.out.println(".........................");

                //**the following functionalities should only be able to be used by customers**
                System.out.println("10. Search Flights");
                System.out.println(".........................");
                System.out.println(".........................");

                //**the following functionalities should ony be able to be used by Pilots**
                System.out.println("15. Maintenace Request");
                System.out.println(".........................");
                System.out.println(".........................");

               //**the following functionalities should ony be able to be used by Technicians**
                System.out.println(".........................");
                System.out.println(".........................");

                System.out.println("20. Log out");
                switch (readChoice()){
                   case 1: feature1(esql); break;
                   case 2: feature2(esql); break;
                   case 3: feature3(esql); break;
                   case 4: feature4(esql); break;
                   case 5: feature5(esql); break;
                   case 6: feature6(esql); break;




                   case 20: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
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
         
         // Basic escaping for single quotes. Not fully secure against SQL injection.
         String safeFirstName = firstName.replace("'", "''");
         String safeLastName = lastName.replace("'", "''");
         String safePassword = password.replace("'", "''");
         String safeFullName = fullName.replace("'", "''");


         if (roleChoice.equals("C")) {
            System.out.println("--- Creating Customer Account ---");
            System.out.print("Enter Gender (M/F/Other): ");
            String gender = in.readLine().trim();
            System.out.print("Enter Date of Birth (YYYY-MM-DD): ");
            String dob = in.readLine().trim(); // User needs to ensure correct format
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
            // dob is generally safe if it's just a date string without quotes

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
            
            // Get the highest existing PilotID number
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
            
            // Get the highest existing TechnicianID number
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
         
         // Basic escaping for single quotes. Not fully secure against SQL injection.
         String safeIdStr = idStr.replace("'", "''");
         String safePassword = password.replace("'", "''");

         if (role.equals("customer")) {
            try {
                Integer.parseInt(idStr); // Basic validation for Customer ID
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

   public static void feature1(AirlineManagement esql) {}
   public static void feature2(AirlineManagement esql) {}
   public static void feature3(AirlineManagement esql) {}
   public static void feature4(AirlineManagement esql) {}
   public static void feature5(AirlineManagement esql) {}
   public static void feature6(AirlineManagement esql) {}
  


}//end AirlineManagement

