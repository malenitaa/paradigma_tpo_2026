package ar.edu.uade.poo.grupo3.tpo.vista;

import ar.edu.uade.poo.grupo3.tpo.modelo.enums.TipoSector;
import ar.edu.uade.poo.grupo3.tpo.modelo.infraestructura.Sector;
import ar.edu.uade.poo.grupo3.tpo.modelo.infraestructura.SectorFactory;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

/**
 * Formulario para cargar un sector. Se reutiliza en el alta y en la modificación de sedes.
 * Según el tipo elegido muestra filas y asientos por fila (numerado) o capacidad (general).
 */
public class FormularioSector extends JPanel {

    private final JTextField codigo = Estilo.campo();
    private final JTextField nombre = Estilo.campo();
    private final JComboBox<TipoSector> tipo = new JComboBox<>(TipoSector.values());
    private final JSpinner piso = spinner(0, 0, 10, 1);
    private final JSpinner distancia = spinner(10, 0, 500, 1);
    private final JSpinner filas = spinner(5, 1, 26, 1);
    private final JSpinner asientosPorFila = spinner(20, 1, 100, 1);
    private final JSpinner capacidad = spinner(500, 1, 100000, 50);
    private final JLabel lblFilas = Estilo.etiqueta("Filas (A, B, C…)");
    private final JLabel lblAsientos = Estilo.etiqueta("Asientos por fila");
    private final JLabel lblCapacidad = Estilo.etiqueta("Capacidad");

    public FormularioSector() {
        super(new GridBagLayout());
        setOpaque(false);
        tipo.setPreferredSize(new Dimension(180, 36));
        codigo.setToolTipText("Código corto, por ejemplo FLR, 100 o CAMPO");

        agregar(Estilo.etiqueta("Código"), 0, 0);
        agregar(codigo, 0, 1);
        agregar(Estilo.etiqueta("Nombre"), 1, 0);
        agregar(nombre, 1, 1);
        agregar(Estilo.etiqueta("Tipo"), 2, 0);
        agregar(tipo, 2, 1);

        agregar(Estilo.etiqueta("Piso"), 0, 2);
        agregar(piso, 0, 3);
        agregar(Estilo.etiqueta("Distancia al escenario (m)"), 1, 2);
        agregar(distancia, 1, 3);

        agregar(lblFilas, 0, 4);
        agregar(filas, 0, 5);
        agregar(lblAsientos, 1, 4);
        agregar(asientosPorFila, 1, 5);
        agregar(lblCapacidad, 2, 4);
        agregar(capacidad, 2, 5);

        tipo.addActionListener(e -> actualizarCampos());
        actualizarCampos();
    }

    private void actualizarCampos() {
        boolean numerado = tipo.getSelectedItem() == TipoSector.NUMERADO;
        filas.setEnabled(numerado);
        asientosPorFila.setEnabled(numerado);
        capacidad.setEnabled(!numerado);
        lblFilas.setEnabled(numerado);
        lblAsientos.setEnabled(numerado);
        lblCapacidad.setEnabled(!numerado);
    }

    /** Crea el sector con la fábrica. Si un dato es inválido lanza IllegalArgumentException. */
    public Sector crearSector() {
        return SectorFactory.crear((TipoSector) tipo.getSelectedItem(), codigo.getText(), nombre.getText(),
                (Integer) piso.getValue(), ((Integer) distancia.getValue()).doubleValue(),
                (Integer) filas.getValue(), (Integer) asientosPorFila.getValue(), (Integer) capacidad.getValue());
    }

    public void limpiar() {
        codigo.setText("");
        nombre.setText("");
        codigo.requestFocusInWindow();
    }

    @Override
    public void setEnabled(boolean habilitado) {
        super.setEnabled(habilitado);
        for (JComponent c : List.of(codigo, nombre, tipo, piso, distancia)) {
            c.setEnabled(habilitado);
        }
        if (habilitado) {
            actualizarCampos();
        } else {
            filas.setEnabled(false);
            asientosPorFila.setEnabled(false);
            capacidad.setEnabled(false);
        }
    }

    private void agregar(JComponent componente, int columna, int fila) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = columna;
        c.gridy = fila;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(fila % 2 == 0 ? 8 : 4, 0, 0, columna < 2 ? 12 : 0);
        add(componente, c);
    }

    private static JSpinner spinner(int valor, int minimo, int maximo, int paso) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(valor, minimo, maximo, paso));
        spinner.setPreferredSize(new Dimension(120, 36));
        spinner.setBorder(Estilo.bordeCampo());
        return spinner;
    }

    // ------------------------------------------------------------------ tabla de sectores compartida

    public static DefaultTableModel modeloSectores() {
        return Estilo.modeloTabla("Código", "Nombre", "Tipo", "Piso", "Distancia (m)", "Capacidad", "Precio x");
    }

    public static javax.swing.JTable tablaSectores(DefaultTableModel modelo) {
        javax.swing.JTable tabla = Estilo.tabla(modelo);
        Estilo.anchos(tabla, 70, 150, 130, 50, 100, 90, 80);
        return tabla;
    }

    public static void llenar(DefaultTableModel modelo, List<Sector> sectores) {
        modelo.setRowCount(0);
        for (Sector s : sectores) {
            modelo.addRow(new Object[]{s.getCodigo(), s.getNombre(), s.getTipo().toString(), s.getPiso(),
                    (int) s.getDistanciaEscenario(), s.getCapacidad(),
                    String.format("× %.2f", s.calcularMultiplicador())});
        }
    }
}
