import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class OrdersPanel extends JPanel {
    private QdreonApp app;
    private ShopService shopService;
    private JTable ordersTable;
    private DefaultTableModel tableModel;

    public OrdersPanel(QdreonApp app) {
        this.app = app;
        this.shopService = app.getShopService();
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        // Header
        JPanel headerPanel = new JPanel();
        JLabel titleLabel = new JLabel("Order Fulfillment");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Order ID", "Customer", "Date", "Total (₱)", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        ordersTable = new JTable(tableModel);
        ordersTable.setRowHeight(30);
        loadOrderData();

        JScrollPane scrollPane = new JScrollPane(ordersTable);
        add(scrollPane, BorderLayout.CENTER);

        // Buttons
        JPanel buttonsPanel = new JPanel();
        JButton viewBtn = new JButton("View Details");
        JButton updateBtn = new JButton("Update Status");
        JButton backBtn = new JButton("Back");

        viewBtn.addActionListener(e -> viewOrderDetails());
        updateBtn.addActionListener(e -> updateOrderStatus());
        backBtn.addActionListener(e -> app.showPanel("ADMIN_DASHBOARD"));

        buttonsPanel.add(viewBtn);
        buttonsPanel.add(updateBtn);
        buttonsPanel.add(backBtn);

        add(buttonsPanel, BorderLayout.SOUTH);
    }

    private void loadOrderData() {
        tableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<Order> orders = shopService.getAllOrders();
        
        for (Order order : orders) {
            Object[] row = {
                order.getId(),
                order.getCustomerName(),
                sdf.format(order.getDate()),
                String.format("%.2f", order.getTotal()),
                order.getStatus()
            };
            tableModel.addRow(row);
        }
    }

    private void viewOrderDetails() {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an order");
            return;
        }

        String orderId = (String) tableModel.getValueAt(selectedRow, 0);
        Order order = shopService.getAllOrders().stream()
            .filter(o -> o.getId().equals(orderId))
            .findFirst()
            .orElse(null);

        if (order != null) {
            StringBuilder details = new StringBuilder();
            details.append("Order ID: ").append(order.getId()).append("\n");
            details.append("Customer: ").append(order.getCustomerName()).append("\n");
            details.append("Address: ").append(order.getShippingAddress()).append("\n");
            details.append("Payment: ").append(order.getPaymentMethod()).append("\n");
            details.append("Status: ").append(order.getStatus()).append("\n\n");
            details.append("Items:\n");
            
            for (CartItem item : order.getItems()) {
                details.append("  - ").append(item.getProduct().getName())
                       .append(" x").append(item.getQuantity())
                       .append(" = ₱").append(String.format("%.2f", item.getSubtotal()))
                       .append("\n");
            }
            
            details.append("\nTotal: ₱").append(String.format("%.2f", order.getTotal()));

            JTextArea textArea = new JTextArea(details.toString());
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(400, 300));

            JOptionPane.showMessageDialog(this, scrollPane, "Order Details", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void updateOrderStatus() {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an order");
            return;
        }

        String orderId = (String) tableModel.getValueAt(selectedRow, 0);
        String[] statuses = {"Pending", "To Ship", "Shipping", "Completed"};
        
        String newStatus = (String) JOptionPane.showInputDialog(
            this,
            "Select new status:",
            "Update Order Status",
            JOptionPane.QUESTION_MESSAGE,
            null,
            statuses,
            statuses[0]
        );

        if (newStatus != null) {
            shopService.updateOrderStatus(orderId, newStatus);
            loadOrderData();
            JOptionPane.showMessageDialog(this, "Order status updated to: " + newStatus);
        }
    }
}