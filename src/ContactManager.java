//Use command prompt finally by typing this prompts in the place you saved and type the two commands
//javac -encoding UTF-8 ContactManager.java
//java ContactManager
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class ContactManager extends JFrame {

    // ── Data ────────────────────────────────────────────────────────────
    static class Contact implements Serializable {
        private static final long serialVersionUID = 1L;
        String name, phone, email;
        Contact(String name, String phone, String email) {
            this.name = name; this.phone = phone; this.email = email;
        }
    }

    private final List<Contact> contacts = new ArrayList<>();
    private final String DATA_FILE = "contacts.dat";

    // ── Table ────────────────────────────────────────────────────────────
    private final String[] COLUMNS = {"Name", "Phone", "Email"};
    private DefaultTableModel tableModel;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;

    // ── Input fields ─────────────────────────────────────────────────────
    private JTextField nameField, phoneField, emailField, searchField;
    private JButton addBtn, updateBtn, deleteBtn, clearBtn;
    private JLabel statusLabel;

    public ContactManager() {
        setTitle("Contact Management System — PRODIGY_SD_03");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(780, 560));
        loadContacts();
        buildUI();
        refreshTable();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ── UI Construction ───────────────────────────────────────────────────
    private void buildUI() {
        setLayout(new BorderLayout(0, 0));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 64, 175));
        header.setBorder(new EmptyBorder(14, 20, 14, 20));
        JLabel title = new JLabel("📋  Contact Management System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("PRODIGY_SD_03");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(147, 197, 253));
        header.add(title, BorderLayout.WEST);
        header.add(sub, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── Left panel: form ─────────────────────────────────────────────
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 0, 1, new Color(220, 220, 220)),
            new EmptyBorder(20, 20, 20, 20)
        ));
        formPanel.setPreferredSize(new Dimension(260, 0));
        formPanel.setBackground(new Color(248, 250, 252));

        JLabel formTitle = new JLabel("Contact Details");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        nameField  = styledField("Full name");
        phoneField = styledField("Phone number");
        emailField = styledField("Email address");

        addBtn    = styledButton("Add Contact",    new Color(22, 163, 74),   Color.WHITE);
        updateBtn = styledButton("Update Contact", new Color(37, 99, 235),   Color.WHITE);
        deleteBtn = styledButton("Delete Contact", new Color(220, 38, 38),   Color.WHITE);
        clearBtn  = styledButton("Clear Fields",   new Color(100, 116, 139), Color.WHITE);

        updateBtn.setEnabled(false);
        deleteBtn.setEnabled(false);

        addBtn.addActionListener(e -> addContact());
        updateBtn.addActionListener(e -> updateContact());
        deleteBtn.addActionListener(e -> deleteContact());
        clearBtn.addActionListener(e -> clearFields());

        formPanel.add(formTitle);
        formPanel.add(Box.createVerticalStrut(16));
        formPanel.add(fieldLabel("Name"));
        formPanel.add(nameField);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(fieldLabel("Phone"));
        formPanel.add(phoneField);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(fieldLabel("Email"));
        formPanel.add(emailField);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(addBtn);
        formPanel.add(Box.createVerticalStrut(6));
        formPanel.add(updateBtn);
        formPanel.add(Box.createVerticalStrut(6));
        formPanel.add(deleteBtn);
        formPanel.add(Box.createVerticalStrut(6));
        formPanel.add(clearBtn);
        formPanel.add(Box.createVerticalGlue());

        // ── Right panel: table ───────────────────────────────────────────
        JPanel rightPanel = new JPanel(new BorderLayout(0, 0));
        rightPanel.setBackground(Color.WHITE);

        // Search bar
        JPanel searchBar = new JPanel(new BorderLayout(8, 0));
        searchBar.setBorder(new EmptyBorder(12, 16, 12, 16));
        searchBar.setBackground(Color.WHITE);
        JLabel searchIcon = new JLabel("🔍");
        searchField = styledField("Search by name...");
        searchField.setBorder(new CompoundBorder(
            new LineBorder(new Color(203, 213, 225), 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterTable(); }
            public void removeUpdate(DocumentEvent e) { filterTable(); }
            public void changedUpdate(DocumentEvent e) { filterTable(); }
        });
        searchBar.add(searchIcon, BorderLayout.WEST);
        searchBar.add(searchField, BorderLayout.CENTER);
        rightPanel.add(searchBar, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(32);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(241, 245, 249));
        table.setSelectionBackground(new Color(219, 234, 254));
        table.setSelectionForeground(new Color(30, 64, 175));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(241, 245, 249));
        table.getTableHeader().setBorder(new MatteBorder(0, 0, 1, 0, new Color(203, 213, 225)));
        table.setFillsViewportHeight(true);

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(180);
        table.getColumnModel().getColumn(1).setPreferredWidth(130);
        table.getColumnModel().getColumn(2).setPreferredWidth(220);

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // Row selection → populate form
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelectedRow();
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        rightPanel.add(scroll, BorderLayout.CENTER);

        // Status bar
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(100, 116, 139));
        statusLabel.setBorder(new EmptyBorder(6, 16, 6, 16));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(248, 250, 252));
        rightPanel.add(statusLabel, BorderLayout.SOUTH);

        // ── Assemble ──────────────────────────────────────────────────────
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, formPanel, rightPanel);
        split.setDividerSize(1);
        split.setDividerLocation(260);
        split.setEnabled(false);
        add(split, BorderLayout.CENTER);
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private JTextField styledField(String placeholder) {
        JTextField f = new JTextField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(new CompoundBorder(
            new LineBorder(new Color(203, 213, 225), 1, true),
            new EmptyBorder(7, 10, 7, 10)
        ));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.setToolTipText(placeholder);
        return f;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(new Color(71, 85, 105));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JButton styledButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setBorder(new CompoundBorder(
            new LineBorder(bg.darker(), 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        return b;
    }

    // ── Table operations ──────────────────────────────────────────────────
    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Contact c : contacts) {
            tableModel.addRow(new Object[]{c.name, c.phone, c.email});
        }
        setStatus(contacts.size() + " contact(s) total");
    }

    private void filterTable() {
        String text = searchField.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 0));
        }
    }

    private void loadSelectedRow() {
        int row = table.getSelectedRow();
        if (row < 0) {
            updateBtn.setEnabled(false);
            deleteBtn.setEnabled(false);
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        Contact c = contacts.get(modelRow);
        nameField.setText(c.name);
        phoneField.setText(c.phone);
        emailField.setText(c.email);
        updateBtn.setEnabled(true);
        deleteBtn.setEnabled(true);
        addBtn.setEnabled(false);
    }

    // ── CRUD ───────────────────────────────────────────────────────────────
    private void addContact() {
        String name  = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();

        if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please fill in all fields.", "Missing Fields",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        contacts.add(new Contact(name, phone, email));
        saveContacts();
        refreshTable();
        clearFields();
        setStatus("✅  Contact '" + name + "' added.");
    }

    private void updateContact() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int modelRow = table.convertRowIndexToModel(row);

        String name  = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();

        if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please fill in all fields.", "Missing Fields",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        Contact c = contacts.get(modelRow);
        c.name = name; c.phone = phone; c.email = email;
        saveContacts();
        refreshTable();
        clearFields();
        setStatus("✏️  Contact updated.");
    }

    private void deleteContact() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int modelRow = table.convertRowIndexToModel(row);
        String name = contacts.get(modelRow).name;

        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete contact '" + name + "'?", "Confirm Delete",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            contacts.remove(modelRow);
            saveContacts();
            refreshTable();
            clearFields();
            setStatus("🗑️  Contact '" + name + "' deleted.");
        }
    }

    private void clearFields() {
        nameField.setText("");
        phoneField.setText("");
        emailField.setText("");
        table.clearSelection();
        addBtn.setEnabled(true);
        updateBtn.setEnabled(false);
        deleteBtn.setEnabled(false);
        nameField.requestFocusInWindow();
    }

    private void setStatus(String msg) {
        statusLabel.setText(msg);
    }

    // ── Persistence ────────────────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private void loadContacts() {
        File f = new File(DATA_FILE);
        if (!f.exists()) return;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(f))) {
            List<Contact> loaded = (List<Contact>) in.readObject();
            contacts.addAll(loaded);
        } catch (Exception e) {
            System.err.println("Could not load contacts: " + e.getMessage());
        }
    }

    private void saveContacts() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            out.writeObject(contacts);
        } catch (IOException e) {
            System.err.println("Could not save contacts: " + e.getMessage());
        }
    }

    // ── Main ───────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ContactManager::new);
    }
}
