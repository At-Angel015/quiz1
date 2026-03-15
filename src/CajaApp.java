
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CajaApp extends JFrame {

    // ── Variables globales ─────────────────────────────────────────────

    private final int[] valores = {
            100000, 50000, 20000, 10000, 5000,
            2000, 1000, 500, 200, 100
    };

    private final int[] stock = new int[valores.length];
    private final String[] tipo = new String[valores.length];

    private JComboBox<String> comboValores;
    private JTextField txtCantidad;
    private JTextField txtCambio;
    private DefaultTableModel modelo;
    private JTextArea areaDetalle;

    // ── Constructor ─────────────────────────────────────────────────────

    public CajaApp() {
        iniciarDatos();
        crearInterfaz();
    }

    // ── Inicializar datos ───────────────────────────────────────────────

    private void iniciarDatos() {
        for (int i = 0; i < valores.length; i++) {
            stock[i] = 0;
            tipo[i] = (valores[i] >= 1000) ? "Billete" : "Moneda";
        }
    }

    // ── Construcción de la interfaz ─────────────────────────────────────

    private void crearInterfaz() {

        setTitle("Sistema de Caja");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        JPanel panelSuperior = new JPanel(new GridBagLayout());
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Combo de valores
        gbc.gridx = 0;
        gbc.gridy = 0;
        panelSuperior.add(new JLabel("Valor:"), gbc);

        gbc.gridx = 1;
        String[] listaValores = new String[valores.length];
        for (int i = 0; i < valores.length; i++) {
            listaValores[i] = String.valueOf(valores[i]);
        }

        comboValores = new JComboBox<>(listaValores);
        panelSuperior.add(comboValores, gbc);

        // Actualizar stock
        gbc.gridx = 0;
        gbc.gridy = 1;
        JButton btnActualizar = new JButton("Actualizar Stock");
        panelSuperior.add(btnActualizar, gbc);

        gbc.gridx = 1;
        txtCantidad = new JTextField(10);
        panelSuperior.add(txtCantidad, gbc);

        // Calcular cambio
        gbc.gridx = 0;
        gbc.gridy = 2;
        panelSuperior.add(new JLabel("Valor a devolver:"), gbc);

        gbc.gridx = 1;
        txtCambio = new JTextField(10);
        panelSuperior.add(txtCambio, gbc);

        gbc.gridx = 2;
        JButton btnCalcular = new JButton("Calcular Cambio");
        panelSuperior.add(btnCalcular, gbc);

        add(panelSuperior, BorderLayout.NORTH);

        // Tabla
        String[] columnas = {"Cantidad", "Tipo", "Valor"};
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tabla = new JTable(modelo);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // Área detalle
        areaDetalle = new JTextArea(6, 40);
        areaDetalle.setEditable(false);
        areaDetalle.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollArea = new JScrollPane(areaDetalle);
        scrollArea.setBorder(
                BorderFactory.createTitledBorder("Detalle del Cambio"));
        add(scrollArea, BorderLayout.SOUTH);

        // Eventos
        btnActualizar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actualizarStock();
            }
        });

        btnCalcular.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calcularCambio();
            }
        });

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ── Actualizar stock ────────────────────────────────────────────────

    private void actualizarStock() {

        int index = comboValores.getSelectedIndex();
        String texto = txtCantidad.getText().trim();

        if (texto.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Ingrese la cantidad.");
            return;
        }

        try {
            int cantidad = Integer.parseInt(texto);

            if (cantidad < 0) {
                JOptionPane.showMessageDialog(this,
                        "No puede ser negativo.");
                return;
            }

            stock[index] = cantidad;
            txtCantidad.setText("");

            JOptionPane.showMessageDialog(this,
                    "Stock actualizado correctamente.");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Ingrese un número válido.");
        }
    }

    // ── Calcular cambio ─────────────────────────────────────────────────

    private void calcularCambio() {

        String texto = txtCambio.getText().trim();

        if (texto.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Ingrese el valor.");
            return;
        }

        try {

            int restante = Integer.parseInt(texto);

            if (restante <= 0) {
                JOptionPane.showMessageDialog(this,
                        "Debe ser mayor que 0.");
                return;
            }

            modelo.setRowCount(0);
            areaDetalle.setText("");

            StringBuilder detalle =
                    new StringBuilder("El cambio se compone de:\n");

            boolean hayCambio = false;

            for (int i = 0; i < valores.length; i++) {

                if (restante <= 0)
                    break;

                int necesarias = restante / valores[i];
                int usadas = Math.min(necesarias, stock[i]);

                if (usadas > 0) {

                    restante -= usadas * valores[i];
                    stock[i] -= usadas;

                    modelo.addRow(new Object[]{
                            usadas,
                            tipo[i],
                            valores[i]
                    });

                    detalle.append(usadas)
                            .append(" ")
                            .append(tipo[i])
                            .append(usadas > 1 ? "s" : "")
                            .append(" de $ ")
                            .append(String.format("%,d", valores[i]))
                            .append("\n");

                    hayCambio = true;
                }
            }

            if (restante > 0) {
                detalle.append("\nNo fue posible completar el cambio.");
            }

            if (!hayCambio) {
                detalle = new StringBuilder(
                        "No hay stock suficiente.");
            }

            areaDetalle.setText(detalle.toString());

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Ingrese un número válido.");
        }
    }

    // ── Main ────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new CajaApp();
            }
        });
    }
}
