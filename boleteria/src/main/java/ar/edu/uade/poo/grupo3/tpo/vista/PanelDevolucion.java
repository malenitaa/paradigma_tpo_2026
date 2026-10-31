package ar.edu.uade.poo.grupo3.tpo.vista;

import ar.edu.uade.poo.grupo3.tpo.controlador.VentaController;
import ar.edu.uade.poo.grupo3.tpo.modelo.enums.MedioPago;
import ar.edu.uade.poo.grupo3.tpo.modelo.eventos.Funcion;
import ar.edu.uade.poo.grupo3.tpo.modelo.excepcion.DevolucionNoPermitidaException;
import ar.edu.uade.poo.grupo3.tpo.modelo.infraestructura.Asiento;
import ar.edu.uade.poo.grupo3.tpo.modelo.infraestructura.Sector;
import ar.edu.uade.poo.grupo3.tpo.modelo.infraestructura.SectorGeneral;
import ar.edu.uade.poo.grupo3.tpo.modelo.infraestructura.SectorNumerado;
import ar.edu.uade.poo.grupo3.tpo.modelo.ventas.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Pantalla de  Devolucion
 */
public class PanelDevolucion extends JPanel implements Pantalla {

    private static final java.time.format.DateTimeFormatter FECHA =
            java.time.format.DateTimeFormatter.ofPattern("EEE dd/MM HH:mm", new java.util.Locale("es", "AR"));
    private static final Color LIBRE = Color.WHITE;
    private static final Color OCUPADO = new Color(0xC9CBD3);
    private static final Color MIO = new Color(0x15803D);
    private final VentaController controlador;
    private final JComboBox<Funcion> funciones = new JComboBox<>();
    private final JComboBox<Sector> sectores = new JComboBox<>();
    private final JComboBox<Cliente> clientes = new JComboBox<>();
    private final JSpinner cantidad = new JSpinner(new SpinnerNumberModel(2, 1, 10, 1));
    private final JLabel detalle = Estilo.subtitulo("Detalle de la devolución.");
    private final JTextField codigoEntrada = Estilo.campo();
    private final JTextField motivoDevolucion = Estilo.campoXL();
    private final JButton verificarEntrada = Estilo.boton("Verificar", Estilo.TipoBoton.SECUNDARIO);
    private final JLabel subtotal = valor();
    private final JButton devolucion = Estilo.boton("Efectuar Devolución", Estilo.TipoBoton.PRIMARIO);
    private final Aviso aviso = new Aviso();
    private boolean cargando;

    public PanelDevolucion(VentaController controlador) {
        super(new BorderLayout());
        this.controlador = controlador;
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));
        add(Estilo.encabezado("Devolución de entradas",
                "Ingrese los siguientes datos para realizar la devolución:"),
                BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(18, 0));
        centro.setOpaque(false);
        centro.add(columnaDevolucion(), BorderLayout.CENTER);
        JComponent resumen = Estilo.tarjeta(columnaResumen());
        resumen.setPreferredSize(new Dimension(330, 0));
        centro.add(resumen, BorderLayout.EAST);
        add(centro, BorderLayout.CENTER);
        add(PanelAltaSede.barraInferior(aviso), BorderLayout.SOUTH);

        devolucion.addActionListener(e -> {
            if (!cargando) {
                efectuarDevolucion();
            }
        });
        verificarEntrada.addActionListener(e -> {
            if (!cargando) {
                verificarEntrada();
            }
        });

    }



    // ------------------------------------------------------------------ armado de la pantalla

    private JComponent columnaDevolucion() {

        JPanel devolucionDatos = new JPanel(new GridLayout(2, 4, 12, 4));
        devolucionDatos.setOpaque(false);
        devolucionDatos.add(Estilo.etiqueta("Codigo de Entrada"));
        devolucionDatos.add(codigoEntrada);
        devolucionDatos.add(verificarEntrada);
        devolucionDatos.add(Box.createVerticalStrut(4));
        devolucionDatos.add(Box.createVerticalStrut(16));

        JPanel devolucionDatos2 = new JPanel(new GridLayout(2, 4, 6, 2));
        devolucionDatos2.setOpaque(false);
        devolucionDatos2.add(PanelDevolucion.alinear(Estilo.etiqueta("Motivo de Devolucion")));;
        devolucionDatos2.add(motivoDevolucion);

        JPanel arriba = new JPanel(new BorderLayout(0, 4));
        arriba.setOpaque(false);
        arriba.add(devolucionDatos, BorderLayout.CENTER);
        arriba.add(devolucionDatos2, BorderLayout.SOUTH);

        JPanel columna = new JPanel(new BorderLayout(0, 14));
        columna.add(arriba, BorderLayout.NORTH);
        return Estilo.tarjeta(columna);
    }

    private JComponent columnaResumen() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(PanelAltaSede.alinear(Estilo.seccion("Tu Devolución")));
        panel.add(Box.createVerticalStrut(8));
        panel.add(PanelAltaSede.alinear(detalle));
        panel.add(Box.createVerticalStrut(2));
        panel.add(Box.createVerticalStrut(12));
        panel.add(Box.createVerticalStrut(10));
        panel.add(Box.createVerticalStrut(4));

        JPanel promo = new JPanel(new BorderLayout(8, 0));
        promo.setOpaque(false);
        promo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        panel.add(PanelAltaSede.alinear(promo));
        panel.add(Box.createVerticalStrut(16));
        panel.add(PanelAltaSede.alinear(fila("Saldo a Reembolsar", subtotal)));
        panel.add(Box.createVerticalStrut(14));
        panel.add(Box.createVerticalStrut(14));
        panel.add(Box.createVerticalStrut(8));
        devolucion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        panel.add(PanelAltaSede.alinear(devolucion));
        panel.add(Box.createVerticalGlue());
        return panel;
    }



    private static JPanel fila(String texto, JLabel valor) {
        JPanel fila = new JPanel(new BorderLayout());
        fila.setOpaque(false);
        fila.add(Estilo.subtitulo(texto), BorderLayout.WEST);
        fila.add(valor, BorderLayout.EAST);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        fila.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        return fila;
    }

    private static JLabel valor() {
        JLabel label = new JLabel("—");
        label.setFont(Estilo.CUERPO);
        return label;
    }

    @Override
    public void alMostrar() {
        cargando = true;
        Object funcionAnterior = funciones.getSelectedItem();
        funciones.removeAllItems();
        controlador.funcionesDisponibles().forEach(funciones::addItem);
        if (funcionAnterior != null) {
            funciones.setSelectedItem(funcionAnterior);
        }
        clientes.removeAllItems();
        controlador.clientes().forEach(clientes::addItem);
        cargando = false;
    }

    private Funcion funcion() {
        return (Funcion) funciones.getSelectedItem();
    }

    private void efectuarDevolucion() {
        Optional<Boolean> entradaValida = verificarEntrada();
        if (!entradaValida.orElse(false)) {
            return;
        }
        try {
            controlador.devolverEntrada(codigoEntrada.getText(), motivoDevolucion.getText());
            aviso.exito("Devolución efectuada: el reembolso se procesará según el medio de pago.");
        } catch (DevolucionNoPermitidaException ex) {
            aviso.error(ex.getMessage());
        }
    }

    private Optional<Boolean> verificarEntrada() {
        try {
            Optional<Entrada> entrada = controlador.buscarEntrada(codigoEntrada.getText());
            if (entrada.isPresent()) {
                aviso.exito("Entrada válida: " );
                subtotal.setText(Estilo.pesos(entrada.get().getPrecio()));
                return Optional.of(true);
            } else {
                aviso.error("Entrada no encontrada.");
                return Optional.empty();
            }
        } catch (Exception ex) {
            aviso.error(ex.getMessage());
            return Optional.empty();
        }
    }


    static <T extends java.awt.Component> T alinear (T componente) {
        if (componente instanceof javax.swing.JComponent j) {
            j.setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        return componente;
    }

}
