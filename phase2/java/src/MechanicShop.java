// cs166 project phase 2 - mechanic shop jdbc client

import java.sql.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.text.SimpleDateFormat;

public class MechanicShop {

   private Connection _connection = null;
   private static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

   public MechanicShop(String dbname, String dbport, String user) throws SQLException {
      System.out.print("connecting to database...");
      try {
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         this._connection = DriverManager.getConnection(url, user, "");
         System.out.println("done.");
      } catch (Exception e) {
         System.err.println("error - unable to connect to database: " + e.getMessage());
         throw new SQLException(e);
      }
   }

   public int executeUpdate(String sql) throws SQLException {
      Statement stmt = this._connection.createStatement();
      int rowCount = stmt.executeUpdate(sql);
      stmt.close();
      return rowCount;
   }

   public int executeQueryAndPrintResult(String query) throws SQLException {
      Statement stmt = this._connection.createStatement();
      ResultSet rs = stmt.executeQuery(query);

      ResultSetMetaData rsmd = rs.getMetaData();
      int numCol = rsmd.getColumnCount();
      int rowCount = 0;

      // print header
      String header = "";
      for (int i = 1; i <= numCol; i++) {
         if (i > 1) header += "\t";
         String colName = rsmd.getColumnName(i);
         header += (colName == null ? "" : colName.trim());
      }
      System.out.println(header);
      System.out.println("--------------------------------------------------");

      // print rows
      while (rs.next()) {
         String row = "";
         for (int i = 1; i <= numCol; i++) {
            if (i > 1) row += "\t";
            String val = rs.getString(i);
            row += (val == null ? "" : val.trim());
         }
         System.out.println(row);
         ++rowCount;
      }
      stmt.close();
      return rowCount;
   }

   public List<List<String>> executeQueryAndReturnResult(String query) throws SQLException {
      Statement stmt = this._connection.createStatement();
      ResultSet rs = stmt.executeQuery(query);

      ResultSetMetaData rsmd = rs.getMetaData();
      int numCol = rsmd.getColumnCount();
      List<List<String>> result = new ArrayList<List<String>>();

      while (rs.next()) {
         List<String> record = new ArrayList<String>();
         for (int i = 1; i <= numCol; i++) {
            String val = rs.getString(i);
            record.add(val == null ? "" : val.trim());
         }
         result.add(record);
      }
      stmt.close();
      return result;
   }

   public int executeQuery(String query) throws SQLException {
      Statement stmt = this._connection.createStatement();
      ResultSet rs = stmt.executeQuery(query);

      int rowCount = 0;
      while (rs.next()) {
         rowCount++;
      }
      stmt.close();
      return rowCount;
   }

   public void cleanup() {
      try {
         if (this._connection != null) {
            this._connection.close();
         }
      } catch (SQLException e) {
         // ignored
      }
   }

   // main

   public static void main(String[] args) {
      if (args.length != 3) {
         System.err.println("usage: java MechanicShop <dbname> <port> <user>");
         return;
      }

      MechanicShop esql = null;
      try {
         try {
            Class.forName("org.postgresql.Driver");
         } catch (ClassNotFoundException e) {
            System.out.println("where is your postgresql jdbc driver?");
            return;
         }

         esql = new MechanicShop(args[0], args[1], args[2]);

         boolean keepon = true;
         while (keepon) {
            System.out.println("\n========== MECHANIC SHOP ==========");
            System.out.println("1. Add Customer");
            System.out.println("2. Add Mechanic");
            System.out.println("3. Add Car");
            System.out.println("4. Insert Service Request");
            System.out.println("5. Close Service Request");
            System.out.println("6. Closed Requests with Bill < $100");
            System.out.println("7. Customers with More Than 20 Cars");
            System.out.println("8. Pre-1995 Cars with < 50k Miles");
            System.out.println("9. Cars with Most Pending Service Requests");
            System.out.println("10. Customer Total Bills");
            System.out.println("11. Exit\n");

            switch (readChoice()) {
               case 1: AddCustomer(esql); break;
               case 2: AddMechanic(esql); break;
               case 3: AddCar(esql); break;
               case 4: InsertServiceRequest(esql); break;
               case 5: CloseServiceRequest(esql); break;
               case 6: ListClosedRequestsUnder100(esql); break;
               case 7: ListCustomersWithMoreThan20Cars(esql); break;
               case 8: ListCarsBefore1995With50kMiles(esql); break;
               case 9: ListKCarsWithMostServices(esql); break;
               case 10: ListCustomerTotalBills(esql); break;
               case 11: keepon = false; break;
               default: System.out.println("invalid choice, try again."); break;
            }
         }
      } catch (Exception e) {
         System.err.println(e.getMessage());
      } finally {
         try {
            if (esql != null) {
               esql.cleanup();
               System.out.println("disconnected. bye!");
            }
         } catch (Exception e) {
            // ignored
         }
      }
   }

   public static int readChoice() {
      int input;
      try {
         System.out.print("choice> ");
         input = Integer.parseInt(in.readLine().trim());
      } catch (Exception e) {
         input = -1;
      }
      return input;
   }

   // input validation helpers

   // check that a string is non-empty and within max length
   public static boolean charCheck(String s, int maxLen) {
      return s != null && !s.trim().isEmpty() && s.length() <= maxLen;
   }

   // check phone matches (###)###-####
   public static boolean phoneCheck(String s) {
      if (s == null || s.length() != 13) return false;
      return s.matches("\\(\\d{3}\\)\\d{3}-\\d{4}");
   }

   // check year is numeric and in valid range
   public static boolean yearCheck(String s) {
      try {
         int year = Integer.parseInt(s.trim());
         return year >= 1970 && year <= 2026;
      } catch (NumberFormatException e) {
         return false;
      }
   }

   // check experience is numeric, 0-100
   public static boolean expCheck(String s) {
      try {
         int exp = Integer.parseInt(s.trim());
         return exp >= 0 && exp <= 100;
      } catch (NumberFormatException e) {
         return false;
      }
   }

   // check vin is 1-16 chars, non-empty
   public static boolean vinCheck(String s) {
      return s != null && !s.trim().isEmpty() && s.trim().length() <= 16;
   }

   // check positive integer (for odometer, k value)
   public static boolean intCheck(String s) {
      try {
         return Integer.parseInt(s.trim()) > 0;
      } catch (NumberFormatException e) {
         return false;
      }
   }

   // check non-negative integer (for IDs that start at 0)
   public static boolean idCheck(String s) {
      try {
         return Integer.parseInt(s.trim()) >= 0;
      } catch (NumberFormatException e) {
         return false;
      }
   }

   // check positive number (for bills)
   public static boolean numCheck(String s) {
      try {
         return Double.parseDouble(s.trim()) >= 0;
      } catch (NumberFormatException e) {
         return false;
      }
   }

   // escape single quotes in user input to avoid breaking sql strings
   public static String esc(String s) {
      return s.replace("'", "''");
   }

   // function 1: add customer

   public static void AddCustomer(MechanicShop esql) {
      try {
         String fname;
         do {
            System.out.print("first name: ");
            fname = in.readLine().trim();
            if (!charCheck(fname, 32)) System.out.println("  invalid. must be 1-32 chars.");
         } while (!charCheck(fname, 32));

         String lname;
         do {
            System.out.print("last name: ");
            lname = in.readLine().trim();
            if (!charCheck(lname, 32)) System.out.println("  invalid. must be 1-32 chars.");
         } while (!charCheck(lname, 32));

         String phone;
         do {
            System.out.print("phone (###)###-####: ");
            phone = in.readLine().trim();
            if (!phoneCheck(phone)) System.out.println("  invalid. use format (###)###-####.");
         } while (!phoneCheck(phone));

         String address;
         do {
            System.out.print("address: ");
            address = in.readLine().trim();
            if (!charCheck(address, 256)) System.out.println("  invalid. must be 1-256 chars.");
         } while (!charCheck(address, 256));

         // next available id
         List<List<String>> res = esql.executeQueryAndReturnResult("SELECT MAX(id) FROM Customer");
         int newId = Integer.parseInt(res.get(0).get(0)) + 1;

         String query = "INSERT INTO Customer (id, fname, lname, phone, address) VALUES ("
            + newId + ", '"
            + esc(fname) + "', '"
            + esc(lname) + "', '"
            + esc(phone) + "', '"
            + esc(address) + "')";
         esql.executeUpdate(query);
         System.out.println("added customer #" + newId + ": " + fname + " " + lname);

      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   // function 2: add mechanic

   public static void AddMechanic(MechanicShop esql) {
      try {
         String fname;
         do {
            System.out.print("first name: ");
            fname = in.readLine().trim();
            if (!charCheck(fname, 32)) System.out.println("  invalid. must be 1-32 chars.");
         } while (!charCheck(fname, 32));

         String lname;
         do {
            System.out.print("last name: ");
            lname = in.readLine().trim();
            if (!charCheck(lname, 32)) System.out.println("  invalid. must be 1-32 chars.");
         } while (!charCheck(lname, 32));

         String expStr;
         do {
            System.out.print("years of experience (0-100): ");
            expStr = in.readLine().trim();
            if (!expCheck(expStr)) System.out.println("  invalid. must be 0-100.");
         } while (!expCheck(expStr));
         int experience = Integer.parseInt(expStr);

         String specialty;
         do {
            System.out.print("specialty: ");
            specialty = in.readLine().trim();
            if (!charCheck(specialty, 64)) System.out.println("  invalid. must be 1-64 chars.");
         } while (!charCheck(specialty, 64));

         // next available id
         List<List<String>> res = esql.executeQueryAndReturnResult("SELECT MAX(id) FROM Mechanic");
         int newId = Integer.parseInt(res.get(0).get(0)) + 1;

         String query = "INSERT INTO Mechanic (id, fname, lname, experience, specialty) VALUES ("
            + newId + ", '"
            + esc(fname) + "', '"
            + esc(lname) + "', "
            + experience + ", '"
            + esc(specialty) + "')";
         esql.executeUpdate(query);
         System.out.println("added mechanic #" + newId + ": " + fname + " " + lname + " (" + specialty + ")");

      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   // function 3: add car

   public static void AddCar(MechanicShop esql) {
      try {
         String vin;
         do {
            System.out.print("vin (up to 16 chars): ");
            vin = in.readLine().trim();
            if (!vinCheck(vin)) System.out.println("  invalid. must be 1-16 chars.");
         } while (!vinCheck(vin));

         // check if vin already exists
         int exists = esql.executeQuery("SELECT * FROM Car WHERE vin = '" + esc(vin) + "'");
         if (exists > 0) {
            System.out.println("that vin already exists in the system.");
            return;
         }

         String make;
         do {
            System.out.print("make: ");
            make = in.readLine().trim();
            if (!charCheck(make, 32)) System.out.println("  invalid. must be 1-32 chars.");
         } while (!charCheck(make, 32));

         String model;
         do {
            System.out.print("model: ");
            model = in.readLine().trim();
            if (!charCheck(model, 32)) System.out.println("  invalid. must be 1-32 chars.");
         } while (!charCheck(model, 32));

         String yearStr;
         do {
            System.out.print("year (1970-2026): ");
            yearStr = in.readLine().trim();
            if (!yearCheck(yearStr)) System.out.println("  invalid. must be 1970-2026.");
         } while (!yearCheck(yearStr));
         int year = Integer.parseInt(yearStr);

         String query = "INSERT INTO Car (vin, make, model, year) VALUES ('"
            + esc(vin) + "', '"
            + esc(make) + "', '"
            + esc(model) + "', "
            + year + ")";
         esql.executeUpdate(query);
         System.out.println("added car: " + year + " " + make + " " + model + " (vin: " + vin + ")");

      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   // function 4: insert service request

   public static void InsertServiceRequest(MechanicShop esql) {
      try {
         // step 1: find customer by last name
         String lname;
         do {
            System.out.print("customer last name: ");
            lname = in.readLine().trim();
            if (!charCheck(lname, 32)) System.out.println("  invalid. must be 1-32 chars.");
         } while (!charCheck(lname, 32));

         List<List<String>> customers = esql.executeQueryAndReturnResult(
            "SELECT id, fname, lname, phone FROM Customer WHERE lname = '" + esc(lname) + "'");

         if (customers.size() == 0) {
            System.out.println("no customers found with last name '" + lname + "'.");
            System.out.print("would you like to add a new customer? (y/n): ");
            String ans = in.readLine().trim();
            if (ans.equalsIgnoreCase("y")) {
               AddCustomer(esql);
            }
            return;
         }

         System.out.println("\nmatching customers:");
         for (int i = 0; i < customers.size(); i++) {
            List<String> c = customers.get(i);
            System.out.println("  " + (i + 1) + ". " + c.get(1) + " " + c.get(2) + " (id: " + c.get(0) + ", phone: " + c.get(3) + ")");
         }

         // pick a customer
         int custChoice;
         do {
            System.out.print("select customer (1-" + customers.size() + "): ");
            String choiceStr = in.readLine().trim();
            try {
               custChoice = Integer.parseInt(choiceStr);
            } catch (NumberFormatException e) {
               custChoice = -1;
            }
            if (custChoice < 1 || custChoice > customers.size())
               System.out.println("  invalid choice.");
         } while (custChoice < 1 || custChoice > customers.size());

         int customerId = Integer.parseInt(customers.get(custChoice - 1).get(0));

         // step 2: pick a car
         List<List<String>> carList = esql.executeQueryAndReturnResult(
            "SELECT c.vin, c.make, c.model, c.year FROM Car c, Owns o " +
            "WHERE o.customer_id = " + customerId + " AND c.vin = o.car_vin");

         String carVin;
         if (carList.size() == 0) {
            System.out.println("this customer doesn't own any cars on file.");
            System.out.print("would you like to add a new car? (y/n): ");
            String ans = in.readLine().trim();
            if (ans.equalsIgnoreCase("y")) {
               AddCar(esql);
               System.out.print("enter the vin you just added: ");
               carVin = in.readLine().trim();
               // link car to customer
               List<List<String>> ownsRes = esql.executeQueryAndReturnResult("SELECT MAX(ownership_id) FROM Owns");
               int newOwnId = Integer.parseInt(ownsRes.get(0).get(0)) + 1;
               esql.executeUpdate("INSERT INTO Owns (ownership_id, customer_id, car_vin) VALUES ("
                  + newOwnId + ", " + customerId + ", '" + esc(carVin) + "')");
               System.out.println("linked car to customer.");
            } else {
               return;
            }
         } else {
            System.out.println("\ncars owned by this customer:");
            for (int i = 0; i < carList.size(); i++) {
               List<String> car = carList.get(i);
               System.out.println("  " + (i + 1) + ". " + car.get(3) + " " + car.get(1) + " " + car.get(2) + " (vin: " + car.get(0) + ")");
            }
            System.out.println("  " + (carList.size() + 1) + ". Add a new car");

            int carChoice;
            do {
               System.out.print("select car (1-" + (carList.size() + 1) + "): ");
               String choiceStr = in.readLine().trim();
               try {
                  carChoice = Integer.parseInt(choiceStr);
               } catch (NumberFormatException e) {
                  carChoice = -1;
               }
               if (carChoice < 1 || carChoice > carList.size() + 1)
                  System.out.println("  invalid choice.");
            } while (carChoice < 1 || carChoice > carList.size() + 1);

            if (carChoice == carList.size() + 1) {
               AddCar(esql);
               System.out.print("enter the vin you just added: ");
               carVin = in.readLine().trim();
               // link car to customer
               List<List<String>> ownsRes = esql.executeQueryAndReturnResult("SELECT MAX(ownership_id) FROM Owns");
               int newOwnId = Integer.parseInt(ownsRes.get(0).get(0)) + 1;
               esql.executeUpdate("INSERT INTO Owns (ownership_id, customer_id, car_vin) VALUES ("
                  + newOwnId + ", " + customerId + ", '" + esc(carVin) + "')");
               System.out.println("linked car to customer.");
            } else {
               carVin = carList.get(carChoice - 1).get(0);
            }
         }

         // step 3: odometer and complaint
         String odoStr;
         do {
            System.out.print("current odometer reading: ");
            odoStr = in.readLine().trim();
            if (!intCheck(odoStr)) System.out.println("  invalid. must be a positive number.");
         } while (!intCheck(odoStr));
         int odometer = Integer.parseInt(odoStr);

         System.out.print("complaint: ");
         String complaint = in.readLine().trim();
         if (complaint.isEmpty()) {
            System.out.println("complaint can't be empty.");
            return;
         }

         // insert with today's date
         List<List<String>> ridRes = esql.executeQueryAndReturnResult("SELECT MAX(rid) FROM Service_Request");
         int newRid = Integer.parseInt(ridRes.get(0).get(0)) + 1;

         String today = new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());

         String query = "INSERT INTO Service_Request (rid, customer_id, car_vin, date, odometer, complain) VALUES ("
            + newRid + ", "
            + customerId + ", '"
            + esc(carVin) + "', '"
            + today + "', "
            + odometer + ", '"
            + esc(complaint) + "')";
         esql.executeUpdate(query);
         System.out.println("created service request #" + newRid);

      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   // function 5: close service request

   public static void CloseServiceRequest(MechanicShop esql) {
      try {
         // look up the request
         String ridStr;
         do {
            System.out.print("service request number (rid): ");
            ridStr = in.readLine().trim();
            if (!idCheck(ridStr)) System.out.println("  invalid. must be a non-negative number.");
         } while (!idCheck(ridStr));
         int rid = Integer.parseInt(ridStr);

         // verify request exists
         int srExists = esql.executeQuery("SELECT * FROM Service_Request WHERE rid = " + rid);
         if (srExists == 0) {
            System.out.println("service request #" + rid + " doesn't exist.");
            return;
         }

         // make sure it's not already closed
         int alreadyClosed = esql.executeQuery("SELECT * FROM Closed_Request WHERE rid = " + rid);
         if (alreadyClosed > 0) {
            System.out.println("service request #" + rid + " is already closed.");
            return;
         }

         // get mechanic id
         String midStr;
         do {
            System.out.print("mechanic employee id: ");
            midStr = in.readLine().trim();
            if (!idCheck(midStr)) System.out.println("  invalid. must be a non-negative number.");
         } while (!idCheck(midStr));
         int mid = Integer.parseInt(midStr);

         // verify mechanic exists
         int mechExists = esql.executeQuery("SELECT * FROM Mechanic WHERE id = " + mid);
         if (mechExists == 0) {
            System.out.println("mechanic #" + mid + " doesn't exist.");
            return;
         }

         // comment and bill
         System.out.print("closing comment: ");
         String comment = in.readLine().trim();

         String billStr;
         do {
            System.out.print("bill amount: ");
            billStr = in.readLine().trim();
            if (!numCheck(billStr)) System.out.println("  invalid. must be a non-negative number.");
         } while (!numCheck(billStr));
         double bill = Double.parseDouble(billStr);

         // insert with today's date
         List<List<String>> widRes = esql.executeQueryAndReturnResult("SELECT MAX(wid) FROM Closed_Request");
         int newWid = Integer.parseInt(widRes.get(0).get(0)) + 1;

         String today = new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());

         // closing date must be >= request date
         List<List<String>> srDate = esql.executeQueryAndReturnResult(
            "SELECT date FROM Service_Request WHERE rid = " + rid);
         String requestDateStr = srDate.get(0).get(0);
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
         java.util.Date requestDate = sdf.parse(requestDateStr);
         java.util.Date closeDate = new java.util.Date(); // today
         if (closeDate.before(requestDate)) {
            System.out.println("error: closing date can't be before the request date (" + requestDateStr + ").");
            return;
         }

         String query = "INSERT INTO Closed_Request (wid, rid, mid, date, comment, bill) VALUES ("
            + newWid + ", "
            + rid + ", "
            + mid + ", '"
            + today + "', '"
            + esc(comment) + "', "
            + bill + ")";
         esql.executeUpdate(query);
         System.out.println("closed service request #" + rid + " (work order #" + newWid + ", bill: $" + bill + ")");

      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   // query 6: closed requests with bill < $100

   public static void ListClosedRequestsUnder100(MechanicShop esql) {
      try {
         System.out.println("\nclosed requests with bill under $100:");
         int rows = esql.executeQueryAndPrintResult(
            "SELECT cr.date, cr.comment, cr.bill " +
            "FROM Closed_Request cr " +
            "WHERE cr.bill < 100");
         System.out.println("(" + rows + " rows)");
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   // query 7: customers with more than 20 cars

   public static void ListCustomersWithMoreThan20Cars(MechanicShop esql) {
      try {
         System.out.println("\ncustomers who own more than 20 cars:");
         int rows = esql.executeQueryAndPrintResult(
            "SELECT c.fname, c.lname " +
            "FROM Customer c, Owns o " +
            "WHERE c.id = o.customer_id " +
            "GROUP BY c.id, c.fname, c.lname " +
            "HAVING COUNT(*) > 20");
         System.out.println("(" + rows + " rows)");
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   // query 8: pre-1995 cars with < 50k miles

   public static void ListCarsBefore1995With50kMiles(MechanicShop esql) {
      try {
         System.out.println("\ncars made before 1995 with less than 50,000 miles on the odometer:");
         int rows = esql.executeQueryAndPrintResult(
            "SELECT DISTINCT c.make, c.model, c.year " +
            "FROM Car c, Service_Request sr " +
            "WHERE c.vin = sr.car_vin " +
            "AND c.year < 1995 " +
            "AND sr.odometer < 50000");
         System.out.println("(" + rows + " rows)");
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   // query 9: top k cars with most pending requests

   public static void ListKCarsWithMostServices(MechanicShop esql) {
      try {
         String kStr;
         do {
            System.out.print("how many results (k)? ");
            kStr = in.readLine().trim();
            if (!intCheck(kStr)) System.out.println("  invalid. must be a positive number.");
         } while (!intCheck(kStr));
         int k = Integer.parseInt(kStr);

         System.out.println("\ntop " + k + " cars with the most pending (open) service requests:");
         int rows = esql.executeQueryAndPrintResult(
            "SELECT c.make, c.model, COUNT(*) as cnt " +
            "FROM Car c, Service_Request sr " +
            "WHERE c.vin = sr.car_vin " +
            "AND sr.rid NOT IN (SELECT rid FROM Closed_Request) " +
            "GROUP BY c.vin, c.make, c.model " +
            "ORDER BY cnt DESC " +
            "LIMIT " + k);
         System.out.println("(" + rows + " rows)");
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   // query 10: customer total bills

   public static void ListCustomerTotalBills(MechanicShop esql) {
      try {
         System.out.println("\ncustomer total bills (highest first):");
         int rows = esql.executeQueryAndPrintResult(
            "SELECT c.fname, c.lname, SUM(cr.bill) as total " +
            "FROM Customer c, Service_Request sr, Closed_Request cr " +
            "WHERE c.id = sr.customer_id " +
            "AND sr.rid = cr.rid " +
            "GROUP BY c.id, c.fname, c.lname " +
            "ORDER BY total DESC");
         System.out.println("(" + rows + " rows)");
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }
}
