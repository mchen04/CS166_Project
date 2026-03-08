/*
 * TestGUI.java - Automated test for MechanicShopGUI
 * Launches the GUI, programmatically interacts with every tab,
 * and verifies results via direct DB queries.
 */

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.*;
import java.sql.*;

public class TestGUI {
   static Connection conn;
   static int passed = 0, failed = 0;

   public static void main(String[] args) throws Exception {
      String db = args.length > 0 ? args[0] : "mechanic_shop";
      String port = args.length > 1 ? args[1] : "5432";
      String user = args.length > 2 ? args[2] : System.getProperty("user.name");

      // direct DB connection for verification
      Class.forName("org.postgresql.Driver");
      conn = DriverManager.getConnection("jdbc:postgresql://localhost:" + port + "/" + db, user, "");

      // launch GUI on EDT and wait
      final MechanicShopGUI[] gui = new MechanicShopGUI[1];
      SwingUtilities.invokeAndWait(() -> {
         try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
         } catch (Exception e) {}
         try {
            gui[0] = new MechanicShopGUI(db, port, user);
            gui[0].setVisible(true);
         } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
         }
      });

      Thread.sleep(2000); // let GUI render

      JFrame frame = gui[0];
      System.out.println("=== MechanicShopGUI Automated Test ===\n");

      // start background thread that auto-dismisses any JOptionPane dialogs
      Thread dismisser = startDialogDismisser();

      // find the tabbed pane
      JTabbedPane tabs = findComponent(frame, JTabbedPane.class);
      check("JTabbedPane found", tabs != null);
      check("Has 10 tabs", tabs.getTabCount() == 10);

      // === TEST 1: Add Customer tab ===
      System.out.println("\n--- Tab 0: Add Customer ---");
      selectTab(tabs, 0);
      JPanel custPanel = (JPanel) tabs.getComponentAt(0);
      JTextField[] custFields = findComponents(custPanel, JTextField.class, 4);
      JButton custBtn = findButtonByText(custPanel, "Add Customer");
      check("Add Customer button found", custBtn != null);

      int custCountBefore = getCount("Customer");
      fillAndClick(custFields, new String[]{"TestFirst", "TestLast", "(999)888-7777", "123 test st, testcity, CA 90000"}, custBtn);
      Thread.sleep(500);
      dismissDialog(frame);
      int custCountAfter = getCount("Customer");
      check("Customer inserted", custCountAfter == custCountBefore + 1);

      // verify in DB
      PreparedStatement ps = conn.prepareStatement("SELECT fname, lname FROM Customer WHERE fname = 'TestFirst' AND lname = 'TestLast'");
      ResultSet rs = ps.executeQuery();
      check("Customer found in DB", rs.next());
      rs.close(); ps.close();

      // === TEST 2: Add Mechanic tab ===
      System.out.println("\n--- Tab 1: Add Mechanic ---");
      selectTab(tabs, 1);
      JPanel mechPanel = (JPanel) tabs.getComponentAt(1);
      JTextField[] mechFields = findComponents(mechPanel, JTextField.class, 4);
      JButton mechBtn = findButtonByText(mechPanel, "Add Mechanic");
      check("Add Mechanic button found", mechBtn != null);

      int mechCountBefore = getCount("Mechanic");
      fillAndClick(mechFields, new String[]{"MechFirst", "MechLast", "15", "engine repair"}, mechBtn);
      Thread.sleep(500);
      dismissDialog(frame);
      int mechCountAfter = getCount("Mechanic");
      check("Mechanic inserted", mechCountAfter == mechCountBefore + 1);

      // === TEST 3: Add Car tab ===
      System.out.println("\n--- Tab 2: Add Car ---");
      selectTab(tabs, 2);
      JPanel carPanel = (JPanel) tabs.getComponentAt(2);
      JTextField[] carFields = findComponents(carPanel, JTextField.class, 4);
      JButton carBtn = findButtonByText(carPanel, "Add Car");
      check("Add Car button found", carBtn != null);

      int carCountBefore = getCount("Car");
      fillAndClick(carFields, new String[]{"TESTVIN12345678", "TestMake", "TestModel", "2020"}, carBtn);
      Thread.sleep(500);
      dismissDialog(frame);
      int carCountAfter = getCount("Car");
      check("Car inserted", carCountAfter == carCountBefore + 1);

      // test duplicate VIN rejection
      fillAndClick(carFields, new String[]{"TESTVIN12345678", "TestMake", "TestModel", "2020"}, carBtn);
      Thread.sleep(500);
      dismissDialog(frame);
      check("Duplicate VIN rejected", getCount("Car") == carCountAfter);

      // === TEST 4: Service Request tab ===
      System.out.println("\n--- Tab 3: New Request ---");
      selectTab(tabs, 3);
      JPanel srPanel = (JPanel) tabs.getComponentAt(3);

      // find the search field and button
      JTextField searchField = findComponents(srPanel, JTextField.class, 1)[0];
      JButton searchBtn = findButtonByText(srPanel, "Search");
      check("Search button found", searchBtn != null);

      // search for a known last name
      SwingUtilities.invokeAndWait(() -> {
         searchField.setText("smith");
      });
      clickButton(searchBtn);
      Thread.sleep(500);

      // find customer table and check it has results
      JTable[] tables = findAllComponents(srPanel, JTable.class);
      check("Found tables in service request panel", tables.length >= 2);
      JTable custTable = tables[0];
      check("Customer search returned results", custTable.getRowCount() > 0);

      // select first customer
      SwingUtilities.invokeAndWait(() -> {
         custTable.setRowSelectionInterval(0, 0);
      });
      Thread.sleep(500);

      // check car table populated
      JTable carTable = tables[1];
      System.out.println("  Cars for selected customer: " + carTable.getRowCount());

      // === TEST 5: Query tabs ===
      // Test Query 6: Bills < $100
      System.out.println("\n--- Tab 5: Bills < $100 ---");
      selectTab(tabs, 5);
      JPanel q6Panel = (JPanel) tabs.getComponentAt(5);
      JButton q6Btn = findButtonByText(q6Panel, "Run Query");
      check("Q6 Run Query button found", q6Btn != null);
      clickButton(q6Btn);
      Thread.sleep(500);
      JTable q6Table = findAllComponents(q6Panel, JTable.class)[0];
      check("Q6 returns results", q6Table.getRowCount() > 0);
      System.out.println("  Q6 rows: " + q6Table.getRowCount());

      // verify against direct DB query
      int dbQ6Count = getCount("Closed_Request WHERE bill < 100");
      check("Q6 count matches DB", q6Table.getRowCount() == dbQ6Count);

      // Test Query 7: >20 Cars
      System.out.println("\n--- Tab 6: > 20 Cars ---");
      selectTab(tabs, 6);
      JPanel q7Panel = (JPanel) tabs.getComponentAt(6);
      JButton q7Btn = findButtonByText(q7Panel, "Run Query");
      clickButton(q7Btn);
      Thread.sleep(500);
      JTable q7Table = findAllComponents(q7Panel, JTable.class)[0];
      check("Q7 returns 5 customers", q7Table.getRowCount() == 5);

      // Test Query 8: Pre-1995
      System.out.println("\n--- Tab 7: Pre-1995 Cars ---");
      selectTab(tabs, 7);
      JPanel q8Panel = (JPanel) tabs.getComponentAt(7);
      JButton q8Btn = findButtonByText(q8Panel, "Run Query");
      clickButton(q8Btn);
      Thread.sleep(500);
      JTable q8Table = findAllComponents(q8Panel, JTable.class)[0];
      check("Q8 returns results", q8Table.getRowCount() > 0);
      System.out.println("  Q8 rows: " + q8Table.getRowCount());

      // Test Query 9: Top K
      System.out.println("\n--- Tab 8: Top K Cars ---");
      selectTab(tabs, 8);
      JPanel q9Panel = (JPanel) tabs.getComponentAt(8);
      JTextField kField = findComponents(q9Panel, JTextField.class, 1)[0];
      JButton q9Btn = findButtonByText(q9Panel, "Run Query");
      SwingUtilities.invokeAndWait(() -> kField.setText("3"));
      clickButton(q9Btn);
      Thread.sleep(500);
      JTable q9Table = findAllComponents(q9Panel, JTable.class)[0];
      check("Q9 returns <= 3 rows", q9Table.getRowCount() <= 3 && q9Table.getRowCount() > 0);
      System.out.println("  Q9 rows: " + q9Table.getRowCount());

      // Test Query 10: Total Bills
      System.out.println("\n--- Tab 9: Total Bills ---");
      selectTab(tabs, 9);
      JPanel q10Panel = (JPanel) tabs.getComponentAt(9);
      JButton q10Btn = findButtonByText(q10Panel, "Run Query");
      clickButton(q10Btn);
      Thread.sleep(500);
      JTable q10Table = findAllComponents(q10Panel, JTable.class)[0];
      check("Q10 returns results", q10Table.getRowCount() > 0);
      System.out.println("  Q10 rows: " + q10Table.getRowCount());

      // === TEST: Close Request tab ===
      System.out.println("\n--- Tab 4: Close Request ---");
      selectTab(tabs, 4);
      JPanel crPanel = (JPanel) tabs.getComponentAt(4);
      JTextField[] crFields = findComponents(crPanel, JTextField.class, 4);
      // crFields[0] = rid, crFields[1] = mid, crFields[2] = comment, crFields[3] = bill
      JButton lookupBtn = findButtonByText(crPanel, "Look Up");
      JButton closeBtn = findButtonByText(crPanel, "Close Service Request");
      check("Look Up button found", lookupBtn != null);
      check("Close button found", closeBtn != null);

      // find an open request
      Statement stmt = conn.createStatement();
      rs = stmt.executeQuery("SELECT rid FROM Service_Request WHERE rid NOT IN (SELECT rid FROM Closed_Request) LIMIT 1");
      int openRid = -1;
      if (rs.next()) openRid = rs.getInt(1);
      rs.close(); stmt.close();
      check("Found open request to test", openRid >= 0);

      if (openRid >= 0) {
         final int testRid = openRid;
         SwingUtilities.invokeAndWait(() -> crFields[0].setText(String.valueOf(testRid)));
         clickButton(lookupBtn);
         Thread.sleep(500);

         // fill close form
         int crCountBefore = getCount("Closed_Request");
         SwingUtilities.invokeAndWait(() -> {
            crFields[1].setText("1");   // mechanic id
            crFields[2].setText("test close comment");
            crFields[3].setText("42.50");
         });
         clickButton(closeBtn);
         Thread.sleep(500);
         dismissDialog(frame);
         int crCountAfter = getCount("Closed_Request");
         check("Close request inserted", crCountAfter == crCountBefore + 1);

         // verify in DB
         ps = conn.prepareStatement("SELECT comment, bill FROM Closed_Request WHERE rid = ?");
         ps.setInt(1, testRid);
         rs = ps.executeQuery();
         if (rs.next()) {
            check("Close comment correct", rs.getString("comment").trim().equals("test close comment"));
            check("Close bill correct", Math.abs(rs.getDouble("bill") - 42.50) < 0.01);
         } else {
            check("Closed request found in DB", false);
         }
         rs.close(); ps.close();
      }

      // === SUMMARY ===
      System.out.println("\n========================================");
      System.out.println("PASSED: " + passed + "  FAILED: " + failed);
      System.out.println("========================================");

      conn.close();
      dismisser.interrupt();

      // close the GUI
      SwingUtilities.invokeAndWait(() -> {
         frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
      });

      System.exit(failed > 0 ? 1 : 0);
   }

   // === helpers ===

   static void check(String name, boolean condition) {
      if (condition) {
         System.out.println("  PASS: " + name);
         passed++;
      } else {
         System.out.println("  FAIL: " + name);
         failed++;
      }
   }

   static int getCount(String tableOrExpr) throws SQLException {
      Statement s = conn.createStatement();
      ResultSet r = s.executeQuery("SELECT COUNT(*) FROM " + tableOrExpr);
      r.next();
      int c = r.getInt(1);
      r.close(); s.close();
      return c;
   }

   static void selectTab(JTabbedPane tabs, int index) throws Exception {
      SwingUtilities.invokeAndWait(() -> tabs.setSelectedIndex(index));
      Thread.sleep(300);
   }

   static void clickButton(JButton btn) throws Exception {
      SwingUtilities.invokeAndWait(() -> btn.doClick());
   }

   static void fillAndClick(JTextField[] fields, String[] values, JButton btn) throws Exception {
      SwingUtilities.invokeAndWait(() -> {
         for (int i = 0; i < Math.min(fields.length, values.length); i++) {
            fields[i].setText(values[i]);
         }
      });
      Thread.sleep(100);
      clickButton(btn);
   }

   // auto-dismiss any JOptionPane dialogs that pop up
   static Thread startDialogDismisser() {
      Thread t = new Thread(() -> {
         while (!Thread.currentThread().isInterrupted()) {
            try {
               Thread.sleep(200);
               SwingUtilities.invokeAndWait(() -> {
                  for (Window w : Window.getWindows()) {
                     if (w instanceof JDialog && w.isVisible()) {
                        JDialog d = (JDialog) w;
                        // only dismiss JOptionPane dialogs (they have OK buttons)
                        if (findComponent(d, JButton.class) != null) {
                           d.dispose();
                        }
                     }
                  }
               });
            } catch (InterruptedException e) {
               break;
            } catch (Exception e) {
               // ignore
            }
         }
      });
      t.setDaemon(true);
      t.start();
      return t;
   }

   static void dismissDialog(JFrame owner) throws Exception {
      // now handled by background dismisser thread
      Thread.sleep(200);
   }

   @SuppressWarnings("unchecked")
   static <T extends Component> T findComponent(Container parent, Class<T> type) {
      for (Component c : parent.getComponents()) {
         if (type.isInstance(c)) return (T) c;
         if (c instanceof Container) {
            T found = findComponent((Container) c, type);
            if (found != null) return found;
         }
      }
      return null;
   }

   @SuppressWarnings("unchecked")
   static <T extends Component> T[] findComponents(Container parent, Class<T> type, int max) {
      java.util.List<T> list = new java.util.ArrayList<>();
      collectComponents(parent, type, list);
      T[] arr = (T[]) java.lang.reflect.Array.newInstance(type, Math.min(list.size(), max));
      for (int i = 0; i < arr.length; i++) arr[i] = list.get(i);
      return arr;
   }

   @SuppressWarnings("unchecked")
   static <T extends Component> T[] findAllComponents(Container parent, Class<T> type) {
      java.util.List<T> list = new java.util.ArrayList<>();
      collectComponents(parent, type, list);
      T[] arr = (T[]) java.lang.reflect.Array.newInstance(type, list.size());
      return list.toArray(arr);
   }

   static <T extends Component> void collectComponents(Container parent, Class<T> type, java.util.List<T> list) {
      for (Component c : parent.getComponents()) {
         if (type.isInstance(c)) list.add(type.cast(c));
         if (c instanceof Container) collectComponents((Container) c, type, list);
      }
   }

   static JButton findButtonByText(Container parent, String text) {
      java.util.List<JButton> buttons = new java.util.ArrayList<>();
      collectComponents(parent, JButton.class, buttons);
      for (JButton b : buttons) {
         if (b.getText() != null && b.getText().equals(text)) return b;
      }
      return null;
   }
}
