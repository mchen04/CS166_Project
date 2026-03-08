/*
 * MechanicShopGUI.java - Swing GUI for CS166 Mechanic Shop (Phase 2)
 * Uses PreparedStatements throughout for SQL injection safety.
 */

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;

public class MechanicShopGUI extends JFrame {
   private Connection conn;
   private JLabel statusLabel;
   private JTextArea sqlArea;

   public static void main(String[] args) {
      if (args.length != 3) {
         System.err.println("Usage: java MechanicShopGUI <dbname> <port> <user>");
         return;
      }
      SwingUtilities.invokeLater(() -> {
         try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
         } catch (Exception e) {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
         }
         try {
            new MechanicShopGUI(args[0], args[1], args[2]).setVisible(true);
         } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
               "Connection failed:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
         }
      });
   }

   public MechanicShopGUI(String dbname, String port, String user) throws Exception {
      Class.forName("org.postgresql.Driver");
      conn = DriverManager.getConnection(
         "jdbc:postgresql://localhost:" + port + "/" + dbname, user, "");

      setTitle("Mechanic Shop Management System");
      setDefaultCloseOperation(EXIT_ON_CLOSE);
      setSize(1020, 720);
      setMinimumSize(new Dimension(820, 600));
      setLocationRelativeTo(null);

      // header
      JLabel header = new JLabel("  \u2699  Mechanic Shop Management");
      header.setFont(new Font("SansSerif", Font.BOLD, 20));
      header.setOpaque(true);
      header.setBackground(new Color(44, 62, 80));
      header.setForeground(Color.WHITE);
      header.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

      // tabs
      JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT);
      tabs.setFont(new Font("SansSerif", Font.PLAIN, 13));

      tabs.addTab(" Add Customer  ", createAddCustomerPanel());
      tabs.addTab(" Add Mechanic  ", createAddMechanicPanel());
      tabs.addTab(" Add Car       ", createAddCarPanel());
      tabs.addTab(" New Request   ", createServiceRequestPanel());
      tabs.addTab(" Close Request ", createCloseRequestPanel());
      tabs.addTab(" Bills < $100  ", createQueryPanel(
         "Closed Requests with Bill Under $100",
         "SELECT cr.date, cr.comment, cr.bill FROM Closed_Request cr WHERE cr.bill < 100 ORDER BY cr.bill",
         new String[]{"Date", "Comment", "Bill ($)"}));
      tabs.addTab(" > 20 Cars     ", createQueryPanel(
         "Customers Who Own More Than 20 Cars",
         "SELECT c.fname, c.lname, COUNT(*) as car_count FROM Customer c " +
         "JOIN Owns o ON c.id = o.customer_id " +
         "GROUP BY c.id, c.fname, c.lname HAVING COUNT(*) > 20 ORDER BY car_count DESC",
         new String[]{"First Name", "Last Name", "Car Count"}));
      tabs.addTab(" Pre-1995 Cars ", createQueryPanel(
         "Cars Before 1995 with Less Than 50,000 Miles",
         "SELECT DISTINCT c.make, c.model, c.year FROM Car c " +
         "JOIN Service_Request sr ON c.vin = sr.car_vin " +
         "WHERE c.year < 1995 AND sr.odometer < 50000 ORDER BY c.year",
         new String[]{"Make", "Model", "Year"}));
      tabs.addTab(" Top K Cars    ", createTopKPanel());
      tabs.addTab(" Total Bills   ", createQueryPanel(
         "Customer Total Bills (Highest First)",
         "SELECT c.fname, c.lname, SUM(cr.bill) as total FROM Customer c " +
         "JOIN Service_Request sr ON c.id = sr.customer_id " +
         "JOIN Closed_Request cr ON sr.rid = cr.rid " +
         "GROUP BY c.id, c.fname, c.lname ORDER BY total DESC",
         new String[]{"First Name", "Last Name", "Total Bill ($)"}));

      // sql log panel
      sqlArea = new JTextArea(4, 60);
      sqlArea.setEditable(false);
      sqlArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
      sqlArea.setBackground(new Color(45, 45, 45));
      sqlArea.setForeground(new Color(0, 230, 118));
      sqlArea.setCaretColor(new Color(0, 230, 118));
      sqlArea.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
      JScrollPane sqlScroll = new JScrollPane(sqlArea);
      sqlScroll.setBorder(BorderFactory.createTitledBorder(
         BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(189, 195, 199)),
         " SQL Log ", TitledBorder.LEFT, TitledBorder.TOP,
         new Font("SansSerif", Font.BOLD, 11)));
      sqlScroll.setPreferredSize(new Dimension(0, 110));

      // status bar
      statusLabel = new JLabel(" Connected to " + dbname);
      statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
      statusLabel.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(189, 195, 199)),
         BorderFactory.createEmptyBorder(6, 10, 6, 10)));

      JPanel bottomPanel = new JPanel(new BorderLayout());
      bottomPanel.add(sqlScroll, BorderLayout.CENTER);
      bottomPanel.add(statusLabel, BorderLayout.SOUTH);

      setLayout(new BorderLayout());
      add(header, BorderLayout.NORTH);
      add(tabs, BorderLayout.CENTER);
      add(bottomPanel, BorderLayout.SOUTH);

      addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent e) {
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
         }
      });
   }

   // ===================== HELPERS =====================

   private JTextField addField(JPanel form, GridBagConstraints gbc, String label, int row) {
      gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
      gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
      JLabel lbl = new JLabel(label + ":");
      lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
      form.add(lbl, gbc);
      JTextField field = new JTextField(25);
      gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
      form.add(field, gbc);
      return field;
   }

   private int getNextId(String table, String col) throws SQLException {
      String sql = "SELECT COALESCE(MAX(" + col + "), -1) + 1 FROM " + table;
      logSQL(sql);
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(sql);
      rs.next();
      int id = rs.getInt(1);
      rs.close(); stmt.close();
      return id;
   }

   private void setStatus(String msg, boolean isError) {
      statusLabel.setText(" " + msg);
      statusLabel.setForeground(isError ? new Color(192, 57, 43) : new Color(39, 174, 96));
   }

   private void showError(String msg) {
      JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
   }

   private void showInfo(String msg) {
      JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
   }

   private DefaultTableModel makeModel(String[] cols) {
      return new DefaultTableModel(cols, 0) {
         public boolean isCellEditable(int r, int c) { return false; }
      };
   }

   private JTable makeTable(DefaultTableModel model) {
      JTable table = new JTable(model);
      table.setAutoCreateRowSorter(true);
      table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      table.setRowHeight(22);
      table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
      return table;
   }

   private String trimNull(String s) {
      return s == null ? "" : s.trim();
   }

   private void logSQL(String sql, Object... params) {
      StringBuilder sb = new StringBuilder();
      if (params.length > 0) {
         int idx = 0;
         for (int i = 0; i < sql.length(); i++) {
            if (sql.charAt(i) == '?' && idx < params.length) {
               Object p = params[idx++];
               if (p instanceof String) sb.append("'").append(p).append("'");
               else sb.append(p);
            } else {
               sb.append(sql.charAt(i));
            }
         }
      } else {
         sb.append(sql);
      }
      String timestamp = new SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
      sqlArea.append("[" + timestamp + "] " + sb.toString() + "\n");
      sqlArea.setCaretPosition(sqlArea.getDocument().getLength());
   }

   // ===================== FUNCTION 1: ADD CUSTOMER =====================

   private JPanel createAddCustomerPanel() {
      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

      JPanel form = new JPanel(new GridBagLayout());
      form.setBorder(BorderFactory.createTitledBorder(
         BorderFactory.createLineBorder(new Color(149, 165, 166)),
         " New Customer ", TitledBorder.LEFT, TitledBorder.TOP,
         new Font("SansSerif", Font.BOLD, 14)));
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.insets = new Insets(8, 12, 8, 12);
      gbc.anchor = GridBagConstraints.WEST;

      JTextField fnameF = addField(form, gbc, "First Name", 0);
      JTextField lnameF = addField(form, gbc, "Last Name", 1);
      JTextField phoneF = addField(form, gbc, "Phone (###)###-####", 2);
      JTextField addrF  = addField(form, gbc, "Address", 3);

      JButton btn = new JButton("Add Customer");
      btn.setFont(new Font("SansSerif", Font.BOLD, 13));
      gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
      gbc.anchor = GridBagConstraints.CENTER;
      gbc.insets = new Insets(18, 12, 8, 12);
      form.add(btn, gbc);

      btn.addActionListener(e -> {
         String fname = fnameF.getText().trim(), lname = lnameF.getText().trim();
         String phone = phoneF.getText().trim(), addr = addrF.getText().trim();
         if (fname.isEmpty() || fname.length() > 32) { showError("First name: 1-32 chars."); return; }
         if (lname.isEmpty() || lname.length() > 32) { showError("Last name: 1-32 chars."); return; }
         if (!phone.matches("\\(\\d{3}\\)\\d{3}-\\d{4}")) { showError("Phone format: (###)###-####"); return; }
         if (addr.isEmpty() || addr.length() > 256) { showError("Address: 1-256 chars."); return; }
         try {
            int id = getNextId("Customer", "id");
            String sql = "INSERT INTO Customer (id, fname, lname, phone, address) VALUES (?, ?, ?, ?, ?)";
            logSQL(sql, id, fname, lname, phone, addr);
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id); ps.setString(2, fname); ps.setString(3, lname);
            ps.setString(4, phone); ps.setString(5, addr);
            ps.executeUpdate(); ps.close();
            setStatus("Added customer #" + id + ": " + fname + " " + lname, false);
            showInfo("Customer #" + id + " added!");
            fnameF.setText(""); lnameF.setText(""); phoneF.setText(""); addrF.setText("");
         } catch (SQLException ex) { showError("Database error: " + ex.getMessage()); }
      });

      panel.add(form, BorderLayout.NORTH);
      return panel;
   }

   // ===================== FUNCTION 2: ADD MECHANIC =====================

   private JPanel createAddMechanicPanel() {
      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

      JPanel form = new JPanel(new GridBagLayout());
      form.setBorder(BorderFactory.createTitledBorder(
         BorderFactory.createLineBorder(new Color(149, 165, 166)),
         " New Mechanic ", TitledBorder.LEFT, TitledBorder.TOP,
         new Font("SansSerif", Font.BOLD, 14)));
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.insets = new Insets(8, 12, 8, 12);
      gbc.anchor = GridBagConstraints.WEST;

      JTextField fnameF = addField(form, gbc, "First Name", 0);
      JTextField lnameF = addField(form, gbc, "Last Name", 1);
      JTextField expF   = addField(form, gbc, "Years of Experience (0-100)", 2);
      JTextField specF  = addField(form, gbc, "Specialty", 3);

      JButton btn = new JButton("Add Mechanic");
      btn.setFont(new Font("SansSerif", Font.BOLD, 13));
      gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
      gbc.anchor = GridBagConstraints.CENTER;
      gbc.insets = new Insets(18, 12, 8, 12);
      form.add(btn, gbc);

      btn.addActionListener(e -> {
         String fname = fnameF.getText().trim(), lname = lnameF.getText().trim();
         String expStr = expF.getText().trim(), spec = specF.getText().trim();
         if (fname.isEmpty() || fname.length() > 32) { showError("First name: 1-32 chars."); return; }
         if (lname.isEmpty() || lname.length() > 32) { showError("Last name: 1-32 chars."); return; }
         int exp;
         try { exp = Integer.parseInt(expStr); } catch (Exception ex) { showError("Experience must be numeric."); return; }
         if (exp < 0 || exp > 100) { showError("Experience: 0-100 years."); return; }
         if (spec.isEmpty() || spec.length() > 64) { showError("Specialty: 1-64 chars."); return; }
         try {
            int id = getNextId("Mechanic", "id");
            String sql = "INSERT INTO Mechanic (id, fname, lname, experience, specialty) VALUES (?, ?, ?, ?, ?)";
            logSQL(sql, id, fname, lname, exp, spec);
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id); ps.setString(2, fname); ps.setString(3, lname);
            ps.setInt(4, exp); ps.setString(5, spec);
            ps.executeUpdate(); ps.close();
            setStatus("Added mechanic #" + id + ": " + fname + " " + lname + " (" + spec + ")", false);
            showInfo("Mechanic #" + id + " added!");
            fnameF.setText(""); lnameF.setText(""); expF.setText(""); specF.setText("");
         } catch (SQLException ex) { showError("Database error: " + ex.getMessage()); }
      });

      panel.add(form, BorderLayout.NORTH);
      return panel;
   }

   // ===================== FUNCTION 3: ADD CAR =====================

   private JPanel createAddCarPanel() {
      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

      JPanel form = new JPanel(new GridBagLayout());
      form.setBorder(BorderFactory.createTitledBorder(
         BorderFactory.createLineBorder(new Color(149, 165, 166)),
         " New Car ", TitledBorder.LEFT, TitledBorder.TOP,
         new Font("SansSerif", Font.BOLD, 14)));
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.insets = new Insets(8, 12, 8, 12);
      gbc.anchor = GridBagConstraints.WEST;

      JTextField vinF   = addField(form, gbc, "VIN (up to 16 chars)", 0);
      JTextField makeF  = addField(form, gbc, "Make", 1);
      JTextField modelF = addField(form, gbc, "Model", 2);
      JTextField yearF  = addField(form, gbc, "Year (1970-2026)", 3);

      JButton btn = new JButton("Add Car");
      btn.setFont(new Font("SansSerif", Font.BOLD, 13));
      gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
      gbc.anchor = GridBagConstraints.CENTER;
      gbc.insets = new Insets(18, 12, 8, 12);
      form.add(btn, gbc);

      btn.addActionListener(e -> {
         String vin = vinF.getText().trim(), make = makeF.getText().trim();
         String model = modelF.getText().trim(), yearStr = yearF.getText().trim();
         if (vin.isEmpty() || vin.length() > 16) { showError("VIN: 1-16 chars."); return; }
         if (make.isEmpty() || make.length() > 32) { showError("Make: 1-32 chars."); return; }
         if (model.isEmpty() || model.length() > 32) { showError("Model: 1-32 chars."); return; }
         int year;
         try { year = Integer.parseInt(yearStr); } catch (Exception ex) { showError("Year must be numeric."); return; }
         if (year < 1970 || year > 2026) { showError("Year: 1970-2026."); return; }
         try {
            // check duplicate
            String ckSql = "SELECT COUNT(*) FROM Car WHERE vin = ?";
            logSQL(ckSql, vin);
            PreparedStatement ck = conn.prepareStatement(ckSql);
            ck.setString(1, vin);
            ResultSet crs = ck.executeQuery(); crs.next();
            if (crs.getInt(1) > 0) { showError("That VIN already exists."); crs.close(); ck.close(); return; }
            crs.close(); ck.close();

            String insSql = "INSERT INTO Car (vin, make, model, year) VALUES (?, ?, ?, ?)";
            logSQL(insSql, vin, make, model, year);
            PreparedStatement ps = conn.prepareStatement(insSql);
            ps.setString(1, vin); ps.setString(2, make); ps.setString(3, model); ps.setInt(4, year);
            ps.executeUpdate(); ps.close();
            setStatus("Added car: " + year + " " + make + " " + model + " (VIN: " + vin + ")", false);
            showInfo("Car added!");
            vinF.setText(""); makeF.setText(""); modelF.setText(""); yearF.setText("");
         } catch (SQLException ex) { showError("Database error: " + ex.getMessage()); }
      });

      panel.add(form, BorderLayout.NORTH);
      return panel;
   }

   // ===================== FUNCTION 4: INSERT SERVICE REQUEST =====================

   private JPanel createServiceRequestPanel() {
      JPanel panel = new JPanel(new BorderLayout(0, 5));
      panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

      // state
      final int[] customerId = {-1};
      final String[] carVin = {""};

      // === step 1: customer search ===
      JPanel step1 = new JPanel(new BorderLayout(5, 5));
      step1.setBorder(BorderFactory.createTitledBorder("Step 1: Find Customer"));

      JPanel searchRow = new JPanel(new BorderLayout(8, 0));
      searchRow.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      JTextField lnameField = new JTextField();
      JButton searchBtn = new JButton("Search");
      searchRow.add(new JLabel("Last Name: "), BorderLayout.WEST);
      searchRow.add(lnameField, BorderLayout.CENTER);
      JPanel searchBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
      searchBtns.add(searchBtn);
      JButton addCustBtn = new JButton("Add New Customer");
      searchBtns.add(addCustBtn);
      searchRow.add(searchBtns, BorderLayout.EAST);

      DefaultTableModel custModel = makeModel(new String[]{"ID", "First Name", "Last Name", "Phone"});
      JTable custTable = makeTable(custModel);
      JScrollPane custScroll = new JScrollPane(custTable);
      custScroll.setPreferredSize(new Dimension(0, 130));

      step1.add(searchRow, BorderLayout.NORTH);
      step1.add(custScroll, BorderLayout.CENTER);

      // === step 2: car selection ===
      JPanel step2 = new JPanel(new BorderLayout(5, 5));
      step2.setBorder(BorderFactory.createTitledBorder("Step 2: Select Car"));

      DefaultTableModel carModel = makeModel(new String[]{"VIN", "Make", "Model", "Year"});
      JTable carTable = makeTable(carModel);
      JScrollPane carScroll = new JScrollPane(carTable);
      carScroll.setPreferredSize(new Dimension(0, 120));

      JButton addCarBtn = new JButton("Add New Car for This Customer");
      JPanel carBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      carBtnPanel.add(addCarBtn);

      step2.add(carScroll, BorderLayout.CENTER);
      step2.add(carBtnPanel, BorderLayout.SOUTH);

      // === step 3: details ===
      JPanel step3 = new JPanel(new GridBagLayout());
      step3.setBorder(BorderFactory.createTitledBorder("Step 3: Service Details"));
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.insets = new Insets(6, 8, 6, 8);
      gbc.anchor = GridBagConstraints.WEST;

      JTextField odoField = addField(step3, gbc, "Odometer Reading", 0);
      JTextField compField = addField(step3, gbc, "Complaint", 1);

      JButton submitBtn = new JButton("Submit Service Request");
      submitBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
      gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
      gbc.anchor = GridBagConstraints.CENTER;
      gbc.insets = new Insets(12, 8, 6, 8);
      step3.add(submitBtn, gbc);

      // layout
      JPanel stepsPanel = new JPanel();
      stepsPanel.setLayout(new BoxLayout(stepsPanel, BoxLayout.Y_AXIS));
      step1.setAlignmentX(Component.LEFT_ALIGNMENT);
      step2.setAlignmentX(Component.LEFT_ALIGNMENT);
      step3.setAlignmentX(Component.LEFT_ALIGNMENT);
      stepsPanel.add(step1);
      stepsPanel.add(Box.createVerticalStrut(5));
      stepsPanel.add(step2);
      stepsPanel.add(Box.createVerticalStrut(5));
      stepsPanel.add(step3);

      panel.add(new JScrollPane(stepsPanel,
         JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
         JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

      // === event handlers ===

      // search customers by last name
      searchBtn.addActionListener(e -> {
         String lname = lnameField.getText().trim();
         if (lname.isEmpty()) { showError("Enter a last name."); return; }
         custModel.setRowCount(0); carModel.setRowCount(0);
         customerId[0] = -1; carVin[0] = "";
         try {
            String sql = "SELECT id, fname, lname, phone FROM Customer WHERE lname = ?";
            logSQL(sql, lname);
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, lname);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
               custModel.addRow(new Object[]{rs.getInt("id"),
                  trimNull(rs.getString("fname")), trimNull(rs.getString("lname")),
                  trimNull(rs.getString("phone"))});
            }
            rs.close(); ps.close();
            if (custModel.getRowCount() == 0)
               setStatus("No customers found with last name '" + lname + "'", true);
            else
               setStatus("Found " + custModel.getRowCount() + " customer(s)", false);
         } catch (SQLException ex) { showError(ex.getMessage()); }
      });

      // allow pressing enter in search field
      lnameField.addActionListener(e -> searchBtn.doClick());

      // selecting a customer loads their cars
      custTable.getSelectionModel().addListSelectionListener(e -> {
         if (e.getValueIsAdjusting()) return;
         int row = custTable.getSelectedRow();
         if (row < 0) return;
         customerId[0] = (int) custModel.getValueAt(custTable.convertRowIndexToModel(row), 0);
         carModel.setRowCount(0); carVin[0] = "";
         try {
            String carSql = "SELECT c.vin, c.make, c.model, c.year FROM Car c " +
               "JOIN Owns o ON c.vin = o.car_vin WHERE o.customer_id = ?";
            logSQL(carSql, customerId[0]);
            PreparedStatement ps = conn.prepareStatement(carSql);
            ps.setInt(1, customerId[0]);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
               carModel.addRow(new Object[]{trimNull(rs.getString("vin")),
                  trimNull(rs.getString("make")), trimNull(rs.getString("model")),
                  rs.getInt("year")});
            }
            rs.close(); ps.close();
            setStatus("Customer #" + customerId[0] + " owns " + carModel.getRowCount() + " car(s)", false);
         } catch (SQLException ex) { showError(ex.getMessage()); }
      });

      // selecting a car stores the vin
      carTable.getSelectionModel().addListSelectionListener(e -> {
         if (e.getValueIsAdjusting()) return;
         int row = carTable.getSelectedRow();
         if (row < 0) return;
         carVin[0] = (String) carModel.getValueAt(carTable.convertRowIndexToModel(row), 0);
      });

      // add new customer dialog
      addCustBtn.addActionListener(e -> {
         int newId = showAddCustomerDialog();
         if (newId >= 0) searchBtn.doClick();
      });

      // add new car dialog (linked to selected customer)
      addCarBtn.addActionListener(e -> {
         if (customerId[0] < 0) { showError("Select a customer first."); return; }
         String newVin = showAddCarDialog(customerId[0]);
         if (newVin != null) {
            // refresh car list
            carModel.setRowCount(0);
            try {
               PreparedStatement ps = conn.prepareStatement(
                  "SELECT c.vin, c.make, c.model, c.year FROM Car c " +
                  "JOIN Owns o ON c.vin = o.car_vin WHERE o.customer_id = ?");
               ps.setInt(1, customerId[0]);
               ResultSet rs = ps.executeQuery();
               while (rs.next()) {
                  carModel.addRow(new Object[]{trimNull(rs.getString("vin")),
                     trimNull(rs.getString("make")), trimNull(rs.getString("model")),
                     rs.getInt("year")});
               }
               rs.close(); ps.close();
            } catch (SQLException ex) { showError(ex.getMessage()); }
         }
      });

      // submit service request
      submitBtn.addActionListener(e -> {
         if (customerId[0] < 0) { showError("Select a customer first."); return; }
         if (carVin[0].isEmpty()) { showError("Select a car first."); return; }
         String odoStr = odoField.getText().trim();
         String complaint = compField.getText().trim();
         int odometer;
         try { odometer = Integer.parseInt(odoStr); }
         catch (Exception ex) { showError("Odometer must be a number."); return; }
         if (odometer <= 0) { showError("Odometer must be positive."); return; }
         if (complaint.isEmpty()) { showError("Enter a complaint."); return; }

         try {
            int rid = getNextId("Service_Request", "rid");
            String today = new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
            String srSql = "INSERT INTO Service_Request (rid, customer_id, car_vin, date, odometer, complain) " +
               "VALUES (?, ?, ?, ?::date, ?, ?)";
            logSQL(srSql, rid, customerId[0], carVin[0], today, odometer, complaint);
            PreparedStatement ps = conn.prepareStatement(srSql);
            ps.setInt(1, rid); ps.setInt(2, customerId[0]); ps.setString(3, carVin[0]);
            ps.setString(4, today); ps.setInt(5, odometer); ps.setString(6, complaint);
            ps.executeUpdate(); ps.close();
            setStatus("Created service request #" + rid, false);
            showInfo("Service request #" + rid + " created!");
            odoField.setText(""); compField.setText("");
         } catch (SQLException ex) { showError("Database error: " + ex.getMessage()); }
      });

      return panel;
   }

   // dialog to add a customer (used from service request flow)
   private int showAddCustomerDialog() {
      JTextField fnameF = new JTextField(20), lnameF = new JTextField(20);
      JTextField phoneF = new JTextField(20), addrF  = new JTextField(20);
      JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));
      form.add(new JLabel("First Name:")); form.add(fnameF);
      form.add(new JLabel("Last Name:"));  form.add(lnameF);
      form.add(new JLabel("Phone (###)###-####:")); form.add(phoneF);
      form.add(new JLabel("Address:"));    form.add(addrF);
      int res = JOptionPane.showConfirmDialog(this, form, "Add New Customer", JOptionPane.OK_CANCEL_OPTION);
      if (res != JOptionPane.OK_OPTION) return -1;
      String fname = fnameF.getText().trim(), lname = lnameF.getText().trim();
      String phone = phoneF.getText().trim(), addr = addrF.getText().trim();
      if (fname.isEmpty() || fname.length() > 32) { showError("First name: 1-32 chars."); return -1; }
      if (lname.isEmpty() || lname.length() > 32) { showError("Last name: 1-32 chars."); return -1; }
      if (!phone.matches("\\(\\d{3}\\)\\d{3}-\\d{4}")) { showError("Phone: (###)###-####"); return -1; }
      if (addr.isEmpty() || addr.length() > 256) { showError("Address: 1-256 chars."); return -1; }
      try {
         int id = getNextId("Customer", "id");
         String sql = "INSERT INTO Customer (id, fname, lname, phone, address) VALUES (?, ?, ?, ?, ?)";
         logSQL(sql, id, fname, lname, phone, addr);
         PreparedStatement ps = conn.prepareStatement(sql);
         ps.setInt(1, id); ps.setString(2, fname); ps.setString(3, lname);
         ps.setString(4, phone); ps.setString(5, addr);
         ps.executeUpdate(); ps.close();
         setStatus("Added customer #" + id + ": " + fname + " " + lname, false);
         return id;
      } catch (SQLException e) { showError(e.getMessage()); return -1; }
   }

   // dialog to add a car and link to customer (used from service request flow)
   private String showAddCarDialog(int custId) {
      JTextField vinF = new JTextField(20), makeF = new JTextField(20);
      JTextField modelF = new JTextField(20), yearF = new JTextField(20);
      JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));
      form.add(new JLabel("VIN (up to 16 chars):")); form.add(vinF);
      form.add(new JLabel("Make:"));  form.add(makeF);
      form.add(new JLabel("Model:")); form.add(modelF);
      form.add(new JLabel("Year (1970-2026):")); form.add(yearF);
      int res = JOptionPane.showConfirmDialog(this, form, "Add New Car", JOptionPane.OK_CANCEL_OPTION);
      if (res != JOptionPane.OK_OPTION) return null;
      String vin = vinF.getText().trim(), make = makeF.getText().trim();
      String model = modelF.getText().trim(), yearStr = yearF.getText().trim();
      if (vin.isEmpty() || vin.length() > 16) { showError("VIN: 1-16 chars."); return null; }
      if (make.isEmpty() || make.length() > 32) { showError("Make: 1-32 chars."); return null; }
      if (model.isEmpty() || model.length() > 32) { showError("Model: 1-32 chars."); return null; }
      int year;
      try { year = Integer.parseInt(yearStr); } catch (Exception e) { showError("Year must be numeric."); return null; }
      if (year < 1970 || year > 2026) { showError("Year: 1970-2026."); return null; }
      try {
         // check duplicate vin
         String ckSql = "SELECT COUNT(*) FROM Car WHERE vin = ?";
         logSQL(ckSql, vin);
         PreparedStatement ck = conn.prepareStatement(ckSql);
         ck.setString(1, vin); ResultSet crs = ck.executeQuery(); crs.next();
         if (crs.getInt(1) > 0) { showError("VIN already exists."); crs.close(); ck.close(); return null; }
         crs.close(); ck.close();
         // insert car
         String carSql = "INSERT INTO Car (vin, make, model, year) VALUES (?, ?, ?, ?)";
         logSQL(carSql, vin, make, model, year);
         PreparedStatement ps = conn.prepareStatement(carSql);
         ps.setString(1, vin); ps.setString(2, make); ps.setString(3, model); ps.setInt(4, year);
         ps.executeUpdate(); ps.close();
         // create ownership
         int oid = getNextId("Owns", "ownership_id");
         String ownsSql = "INSERT INTO Owns (ownership_id, customer_id, car_vin) VALUES (?, ?, ?)";
         logSQL(ownsSql, oid, custId, vin);
         PreparedStatement ops = conn.prepareStatement(ownsSql);
         ops.setInt(1, oid); ops.setInt(2, custId); ops.setString(3, vin);
         ops.executeUpdate(); ops.close();
         setStatus("Added car " + year + " " + make + " " + model + " linked to customer #" + custId, false);
         return vin;
      } catch (SQLException e) { showError(e.getMessage()); return null; }
   }

   // ===================== FUNCTION 5: CLOSE SERVICE REQUEST =====================

   private JPanel createCloseRequestPanel() {
      JPanel panel = new JPanel(new BorderLayout(0, 10));
      panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

      // lookup section
      JPanel lookupPanel = new JPanel(new BorderLayout(5, 5));
      lookupPanel.setBorder(BorderFactory.createTitledBorder("Look Up Service Request"));
      JPanel lookupRow = new JPanel(new BorderLayout(8, 0));
      lookupRow.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      JTextField ridField = new JTextField(12);
      JButton lookupBtn = new JButton("Look Up");
      lookupRow.add(new JLabel("Request ID: "), BorderLayout.WEST);
      lookupRow.add(ridField, BorderLayout.CENTER);
      lookupRow.add(lookupBtn, BorderLayout.EAST);

      JTextArea detailsArea = new JTextArea(7, 40);
      detailsArea.setEditable(false);
      detailsArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
      detailsArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

      lookupPanel.add(lookupRow, BorderLayout.NORTH);
      lookupPanel.add(new JScrollPane(detailsArea), BorderLayout.CENTER);

      // close section
      JPanel closePanel = new JPanel(new GridBagLayout());
      closePanel.setBorder(BorderFactory.createTitledBorder("Close Request"));
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.insets = new Insets(6, 8, 6, 8);
      gbc.anchor = GridBagConstraints.WEST;

      JTextField midField  = addField(closePanel, gbc, "Mechanic Employee ID", 0);
      JTextField commField = addField(closePanel, gbc, "Comment", 1);
      JTextField billField = addField(closePanel, gbc, "Bill Amount ($)", 2);

      JButton closeBtn = new JButton("Close Service Request");
      closeBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
      closeBtn.setEnabled(false);
      gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
      gbc.anchor = GridBagConstraints.CENTER;
      gbc.insets = new Insets(14, 8, 6, 8);
      closePanel.add(closeBtn, gbc);

      // state
      final int[] validRid = {-1};

      // layout
      JPanel main = new JPanel();
      main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
      lookupPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      closePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      main.add(lookupPanel);
      main.add(Box.createVerticalStrut(10));
      main.add(closePanel);
      panel.add(main, BorderLayout.NORTH);

      // allow pressing enter in rid field
      ridField.addActionListener(e -> lookupBtn.doClick());

      // look up handler
      lookupBtn.addActionListener(e -> {
         String ridStr = ridField.getText().trim();
         int rid;
         try { rid = Integer.parseInt(ridStr); }
         catch (Exception ex) { showError("Request ID must be a number."); return; }
         if (rid < 0) { showError("Request ID must be non-negative."); return; }

         validRid[0] = -1;
         closeBtn.setEnabled(false);
         detailsArea.setText("");

         try {
            // fetch request info
            String lookupSql = "SELECT sr.rid, sr.customer_id, sr.car_vin, sr.date, sr.odometer, sr.complain, " +
               "c.fname, c.lname, car.make, car.model, car.year " +
               "FROM Service_Request sr " +
               "JOIN Customer c ON sr.customer_id = c.id " +
               "JOIN Car car ON sr.car_vin = car.vin " +
               "WHERE sr.rid = ?";
            logSQL(lookupSql, rid);
            PreparedStatement ps = conn.prepareStatement(lookupSql);
            ps.setInt(1, rid);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
               detailsArea.setText("Service request #" + rid + " not found.");
               rs.close(); ps.close(); return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Request #").append(rid).append("\n");
            sb.append("Customer: ").append(trimNull(rs.getString("fname"))).append(" ")
              .append(trimNull(rs.getString("lname"))).append(" (ID: ")
              .append(rs.getInt("customer_id")).append(")\n");
            sb.append("Car: ").append(rs.getInt("year")).append(" ")
              .append(trimNull(rs.getString("make"))).append(" ")
              .append(trimNull(rs.getString("model"))).append(" (VIN: ")
              .append(trimNull(rs.getString("car_vin"))).append(")\n");
            sb.append("Date: ").append(trimNull(rs.getString("date"))).append("\n");
            sb.append("Odometer: ").append(rs.getInt("odometer")).append("\n");
            sb.append("Complaint: ").append(trimNull(rs.getString("complain"))).append("\n");
            rs.close(); ps.close();

            // check if already closed
            String closedSql = "SELECT cr.date, cr.comment, cr.bill, m.fname, m.lname " +
               "FROM Closed_Request cr JOIN Mechanic m ON cr.mid = m.id WHERE cr.rid = ?";
            logSQL(closedSql, rid);
            PreparedStatement ck = conn.prepareStatement(closedSql);
            ck.setInt(1, rid);
            ResultSet crs = ck.executeQuery();
            if (crs.next()) {
               sb.append("\nSTATUS: CLOSED\n");
               sb.append("Closed by: ").append(trimNull(crs.getString("fname"))).append(" ")
                 .append(trimNull(crs.getString("lname"))).append("\n");
               sb.append("Close date: ").append(trimNull(crs.getString("date"))).append("\n");
               sb.append("Comment: ").append(trimNull(crs.getString("comment"))).append("\n");
               sb.append("Bill: $").append(crs.getDouble("bill")).append("\n");
               closeBtn.setEnabled(false);
               setStatus("Request #" + rid + " is already closed", true);
            } else {
               sb.append("\nSTATUS: OPEN");
               validRid[0] = rid;
               closeBtn.setEnabled(true);
               setStatus("Request #" + rid + " is open - ready to close", false);
            }
            crs.close(); ck.close();
            detailsArea.setText(sb.toString());
         } catch (SQLException ex) { showError(ex.getMessage()); }
      });

      // close handler
      closeBtn.addActionListener(e -> {
         if (validRid[0] < 0) { showError("Look up a valid open request first."); return; }
         String midStr = midField.getText().trim();
         String comment = commField.getText().trim();
         String billStr = billField.getText().trim();
         int mid;
         try { mid = Integer.parseInt(midStr); }
         catch (Exception ex) { showError("Mechanic ID must be a number."); return; }
         if (mid < 0) { showError("Mechanic ID must be non-negative."); return; }
         double bill;
         try { bill = Double.parseDouble(billStr); }
         catch (Exception ex) { showError("Bill must be a number."); return; }
         if (bill < 0) { showError("Bill must be non-negative."); return; }

         try {
            // check mechanic exists
            String mechSql = "SELECT COUNT(*) FROM Mechanic WHERE id = ?";
            logSQL(mechSql, mid);
            PreparedStatement mk = conn.prepareStatement(mechSql);
            mk.setInt(1, mid); ResultSet mrs = mk.executeQuery(); mrs.next();
            if (mrs.getInt(1) == 0) { showError("Mechanic #" + mid + " not found."); mrs.close(); mk.close(); return; }
            mrs.close(); mk.close();

            // check close date >= request date
            String dateSql = "SELECT date FROM Service_Request WHERE rid = ?";
            logSQL(dateSql, validRid[0]);
            PreparedStatement dp = conn.prepareStatement(dateSql);
            dp.setInt(1, validRid[0]); ResultSet drs = dp.executeQuery(); drs.next();
            java.sql.Date reqDate = drs.getDate("date");
            drs.close(); dp.close();
            java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
            if (today.before(reqDate)) {
               showError("Close date (today) can't be before request date (" + reqDate + ").");
               return;
            }

            int wid = getNextId("Closed_Request", "wid");
            String closeSql = "INSERT INTO Closed_Request (wid, rid, mid, date, comment, bill) VALUES (?, ?, ?, ?, ?, ?)";
            logSQL(closeSql, wid, validRid[0], mid, today, comment, bill);
            PreparedStatement ps = conn.prepareStatement(closeSql);
            ps.setInt(1, wid); ps.setInt(2, validRid[0]); ps.setInt(3, mid);
            ps.setDate(4, today); ps.setString(5, comment); ps.setDouble(6, bill);
            ps.executeUpdate(); ps.close();

            setStatus("Closed request #" + validRid[0] + " (work order #" + wid + ", bill: $" + bill + ")", false);
            showInfo("Service request #" + validRid[0] + " closed!\nWork order #" + wid + ", bill: $" + bill);
            validRid[0] = -1;
            closeBtn.setEnabled(false);
            midField.setText(""); commField.setText(""); billField.setText("");
            detailsArea.setText("");
         } catch (SQLException ex) { showError("Database error: " + ex.getMessage()); }
      });

      return panel;
   }

   // ===================== QUERIES 6, 7, 8, 10 (reusable) =====================

   private JPanel createQueryPanel(String title, String sql, String[] columns) {
      JPanel panel = new JPanel(new BorderLayout(0, 10));
      panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

      JLabel titleLabel = new JLabel(title);
      titleLabel.setFont(new Font("SansSerif", Font.BOLD, 15));

      DefaultTableModel model = makeModel(columns);
      JTable table = makeTable(model);

      JButton runBtn = new JButton("Run Query");
      runBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
      JLabel countLabel = new JLabel("");
      countLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

      JPanel bottomPanel = new JPanel(new BorderLayout());
      bottomPanel.add(runBtn, BorderLayout.WEST);
      bottomPanel.add(countLabel, BorderLayout.EAST);

      runBtn.addActionListener(e -> {
         model.setRowCount(0);
         try {
            logSQL(sql);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            int count = 0;
            while (rs.next()) {
               Object[] row = new Object[columns.length];
               for (int i = 0; i < columns.length; i++)
                  row[i] = trimNull(rs.getString(i + 1));
               model.addRow(row);
               count++;
            }
            rs.close(); stmt.close();
            countLabel.setText(count + " row(s) returned");
            setStatus("Query complete: " + count + " results", false);
         } catch (SQLException ex) { showError(ex.getMessage()); }
      });

      panel.add(titleLabel, BorderLayout.NORTH);
      panel.add(new JScrollPane(table), BorderLayout.CENTER);
      panel.add(bottomPanel, BorderLayout.SOUTH);
      return panel;
   }

   // ===================== QUERY 9: TOP K CARS =====================

   private JPanel createTopKPanel() {
      JPanel panel = new JPanel(new BorderLayout(0, 10));
      panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

      JLabel titleLabel = new JLabel("Top K Cars with Most Pending Service Requests");
      titleLabel.setFont(new Font("SansSerif", Font.BOLD, 15));

      JPanel inputRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
      JTextField kField = new JTextField(8);
      JButton runBtn = new JButton("Run Query");
      runBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
      inputRow.add(new JLabel("K value:"));
      inputRow.add(kField);
      inputRow.add(runBtn);
      JLabel countLabel = new JLabel("");
      inputRow.add(countLabel);

      JPanel topPanel = new JPanel(new BorderLayout());
      topPanel.add(titleLabel, BorderLayout.NORTH);
      topPanel.add(inputRow, BorderLayout.SOUTH);

      DefaultTableModel model = makeModel(new String[]{"Make", "Model", "Pending Requests"});
      JTable table = makeTable(model);

      kField.addActionListener(e -> runBtn.doClick());
      runBtn.addActionListener(e -> {
         String kStr = kField.getText().trim();
         int k;
         try { k = Integer.parseInt(kStr); }
         catch (Exception ex) { showError("K must be a positive number."); return; }
         if (k <= 0) { showError("K must be a positive number."); return; }

         model.setRowCount(0);
         try {
            String topSql = "SELECT c.make, c.model, COUNT(*) as pending FROM Car c " +
               "JOIN Service_Request sr ON c.vin = sr.car_vin " +
               "WHERE sr.rid NOT IN (SELECT rid FROM Closed_Request) " +
               "GROUP BY c.vin, c.make, c.model ORDER BY pending DESC LIMIT ?";
            logSQL(topSql, k);
            PreparedStatement ps = conn.prepareStatement(topSql);
            ps.setInt(1, k);
            ResultSet rs = ps.executeQuery();
            int count = 0;
            while (rs.next()) {
               model.addRow(new Object[]{trimNull(rs.getString("make")),
                  trimNull(rs.getString("model")), rs.getInt("pending")});
               count++;
            }
            rs.close(); ps.close();
            countLabel.setText("  " + count + " row(s)");
            setStatus("Top " + k + " query complete: " + count + " results", false);
         } catch (SQLException ex) { showError(ex.getMessage()); }
      });

      panel.add(topPanel, BorderLayout.NORTH);
      panel.add(new JScrollPane(table), BorderLayout.CENTER);
      return panel;
   }
}
