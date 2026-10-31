package ar.edu.uade.poo.grupo3.tpo.vista;

import ar.edu.uade.poo.grupo3.tpo.controlador.VentaController;
import ar.edu.uade.poo.grupo3.tpo.modelo.enums.MedioPago;
import ar.edu.uade.poo.grupo3.tpo.modelo.eventos.Funcion;
import ar.edu.uade.poo.grupo3.tpo.modelo.excepcion.DevolucionNoPermitidaException;
import ar.edu.uade.poo.grupo3.tpo.modelo.infraestructura.Asiento;
import ar.edu.uade.poo.grupo3.tpo.modelo.infraestructura.Sector;
import ar.edu.uade.poo.grupo3.tpo.modelo.ventas.*;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.*;

/**
 * Pantalla de venta: se elige función y sector, se marcan asientos en el mapa, se reservan por
 * 5 minutos y se pagan. Al pagar se muestra la factura.
 * Es solo la vista: la compra en curso y el reloj de la reserva los lleva el VentaController.
 */
public class PanelDevolucion extends JPanel implements Pantalla {

    private static final Color LIBRE = Color.WHITE;
    private static final Color OCUPADO = Color.LIGHT_GRAY;
    private static final Color SELECCIONADO = new Color(0x3F51B5);
    private static final Color MIO = new Color(0x2E7D32);

    private final VentaController controlador;
    private final JComboBox<Funcion> funciones = new JComboBox<>();
    private final JComboBox<Sector> sectores = new JComboBox<>();
    private final JComboBox<Cliente> clientes = new JComboBox<>();
    private final JTextField codigoPreventa = new JTextField(10);
    private final JLabel avisoPreventa = Estilo.subtitulo(" ");
    private final JPanel mapa = new JPanel();
    private final JSpinner cantidad = new JSpinner(new SpinnerNumberModel(2, 1, 10, 1));

    private final JLabel detalle = Estilo.subtitulo("Detalle del reembolso.");
    private final JLabel precioUnitario = Estilo.subtitulo(" ");
    private final JCheckBox paqueteVip = new JCheckBox();
    private final JTextField codigoEntrada = new JTextField(10);
    private final JTextArea motivoDevolucion = new JTextArea(4, 20);
    private final JButton aplicarPromo = new JButton("Aplicar");
    private final JLabel reembolso = new JLabel("—");
    private final JLabel descuento = new JLabel("—");
    private final JLabel cargos = new JLabel("—");
    private final JLabel total = new JLabel("—");
    private final JLabel reloj = new JLabel(" ");
    private final JComboBox<MedioPago> medioPago = new JComboBox<>(MedioPago.values());
    private final JButton reservar = new JButton("Reservar");
    private final JButton pagar = new JButton("Pagar");
    private final JButton cancelar = new JButton("Cancelar");
    private final JButton verificarEntrada = new JButton("Verificar Entrada");
    private final Aviso aviso = new Aviso();

    private final Set<Asiento> seleccion = new LinkedHashSet<>();
    //private final Timer timer = new Timer(1000, e -> tic());
    private boolean cargando;

    public PanelDevolucion(VentaController controlador) {
        super(new BorderLayout());
        this.controlador = controlador;
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        add(Estilo.encabezado("Devolución de entradas",
                "Ingresa los datos para proceder con la devolución de entradas:"),
                BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(16, 0));
        centro.add(columnaMapa(), BorderLayout.CENTER);
        JPanel resumen = Estilo.tarjeta(columnaResumen());

        Border lineBorder = BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1);
        motivoDevolucion.setBorder(lineBorder);

        resumen.setPreferredSize(new Dimension(330, 0));
        centro.add(resumen, BorderLayout.EAST);
        add(centro, BorderLayout.CENTER);
        add(Estilo.barraInferior(aviso), BorderLayout.SOUTH);

        verificarEntrada.addActionListener(e -> {
            if (!cargando) {
                verificarEntrada();
            }
        });

        pagar.addActionListener(e -> {
            if (!cargando) {
                efectuarDevolucion();
            }
        });
    }

    // ------------------------------------------------------------------ armado de la pantalla

    private JComponent columnaMapa() {
        JPanel panelCodEntrada = new JPanel(new GridLayout(1, 4, 20, 2));
        panelCodEntrada.add(new JLabel("Codigo de Entrada"));
        panelCodEntrada.add(codigoEntrada);
        panelCodEntrada.add(verificarEntrada);

        JPanel panelMotivo = new JPanel(new GridLayout(2, 3, 20, 2));
        panelMotivo.add(new JLabel("Motivo de la devolución"), BorderLayout.WEST);
        panelMotivo.add(motivoDevolucion, BorderLayout.CENTER);



        JPanel arriba = new JPanel(new BorderLayout(0, 4));
        arriba.add(panelCodEntrada, BorderLayout.CENTER);
        arriba.add(panelMotivo, BorderLayout.SOUTH);


        JPanel columna = new JPanel(new BorderLayout(0, 12));
        columna.add(arriba, BorderLayout.NORTH);
        return Estilo.tarjeta(columna);
    }


    private JComponent columnaResumen() {
        JPanel promo = new JPanel(new BorderLayout(8, 0));


        total.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        reloj.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        reloj.setForeground(SELECCIONADO);


        return Estilo.columna(
                Estilo.seccion("Tu devolución"),
                detalle,
                precioUnitario,

                fila("Monto a reembolsar:", reembolso),
                pagar);
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


    // ------------------------------------------------------------------ datos

    @Override
    public void alMostrar() {

    }

    private Funcion funcion() {
        return (Funcion) funciones.getSelectedItem();
    }

    private Sector sector() {
        return (Sector) sectores.getSelectedItem();
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
                reembolso.setText(Estilo.pesos(entrada.get().getPrecio()));
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
}
