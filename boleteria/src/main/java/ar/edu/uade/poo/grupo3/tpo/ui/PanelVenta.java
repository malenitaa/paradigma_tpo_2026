package ar.edu.uade.poo.grupo3.tpo.ui;

import ar.edu.uade.poo.grupo3.tpo.controlador.Boleteria;
import ar.edu.uade.poo.grupo3.tpo.enums.MedioPago;
import ar.edu.uade.poo.grupo3.tpo.eventos.Funcion;
import ar.edu.uade.poo.grupo3.tpo.excepcion.ReservaVencidaException;
import ar.edu.uade.poo.grupo3.tpo.excepcion.SeleccionInvalidaException;
import ar.edu.uade.poo.grupo3.tpo.infraestructura.Asiento;
import ar.edu.uade.poo.grupo3.tpo.infraestructura.Sector;
import ar.edu.uade.poo.grupo3.tpo.infraestructura.SectorGeneral;
import ar.edu.uade.poo.grupo3.tpo.infraestructura.SectorNumerado;
import ar.edu.uade.poo.grupo3.tpo.ventas.Cliente;
import ar.edu.uade.poo.grupo3.tpo.ventas.Compra;
import ar.edu.uade.poo.grupo3.tpo.ventas.Entrada;
import ar.edu.uade.poo.grupo3.tpo.ventas.Factura;
import ar.edu.uade.poo.grupo3.tpo.ventas.PaqueteVIP;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Pantalla de venta "estilo Ticketmaster": se elige función y sector, se marcan asientos en el mapa,
 * se reservan por 5 minutos y se pagan. Al pagar se muestra la factura.
 */
public class PanelVenta extends JPanel implements Pantalla {

    private static final java.time.format.DateTimeFormatter FECHA =
            java.time.format.DateTimeFormatter.ofPattern("EEE dd/MM HH:mm", new java.util.Locale("es", "AR"));
    private static final Color LIBRE = Color.WHITE;
    private static final Color OCUPADO = new Color(0xC9CBD3);
    private static final Color MIO = new Color(0x15803D);

    private final Boleteria boleteria;
    private final JComboBox<Funcion> funciones = new JComboBox<>();
    private final JComboBox<Sector> sectores = new JComboBox<>();
    private final JComboBox<Cliente> clientes = new JComboBox<>();
    private final JTextField codigoPreventa = Estilo.campo();
    private final JLabel avisoPreventa = Estilo.subtitulo(" ");
    private final JPanel mapa = new JPanel(new GridBagLayout());
    private final JSpinner cantidad = new JSpinner(new SpinnerNumberModel(2, 1, 10, 1));

    private final JLabel detalle = Estilo.subtitulo("Elegí tus asientos en el mapa.");
    private final JLabel precioUnitario = Estilo.subtitulo(" ");
    private final JCheckBox paqueteVip = new JCheckBox();
    private final JTextField codigoPromo = Estilo.campo();
    private final JButton aplicarPromo = Estilo.boton("Aplicar", Estilo.TipoBoton.SECUNDARIO);
    private final JLabel subtotal = valor();
    private final JLabel descuento = valor();
    private final JLabel cargos = valor();
    private final JLabel total = valor();
    private final JLabel reloj = new JLabel(" ");
    private final JComboBox<MedioPago> medioPago = new JComboBox<>(MedioPago.values());
    private final JButton reservar = Estilo.boton("Reservar", Estilo.TipoBoton.PRIMARIO);
    private final JButton pagar = Estilo.boton("Pagar", Estilo.TipoBoton.PRIMARIO);
    private final JButton cancelar = Estilo.boton("Cancelar", Estilo.TipoBoton.SECUNDARIO);
    private final Aviso aviso = new Aviso();

    private final Set<Asiento> seleccion = new LinkedHashSet<>();
    private final Timer timer = new Timer(1000, e -> tic());
    private Compra compra;
    private Duration desfaseReloj = Duration.ZERO;
    private boolean cargando;

    public PanelVenta(Boleteria boleteria) {
        super(new BorderLayout());
        this.boleteria = boleteria;
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));
        add(Estilo.encabezado("Venta de entradas",
                "Elegí la función y el sector, marcá tus asientos y reservalos: tenés 5 minutos para pagar."),
                BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(18, 0));
        centro.setOpaque(false);
        centro.add(columnaMapa(), BorderLayout.CENTER);
        JComponent resumen = Estilo.tarjeta(columnaResumen());
        resumen.setPreferredSize(new Dimension(330, 0));
        centro.add(resumen, BorderLayout.EAST);
        add(centro, BorderLayout.CENTER);
        add(PanelAltaSede.barraInferior(aviso), BorderLayout.SOUTH);

        funciones.addActionListener(e -> {
            if (!cargando) {
                cargarSectores();
            }
        });
        sectores.addActionListener(e -> {
            if (!cargando) {
                seleccion.clear();
                dibujarMapa();
            }
        });
        cantidad.addChangeListener(e -> actualizarResumen());
        paqueteVip.addActionListener(e -> actualizarResumen());
        reservar.addActionListener(e -> reservar());
        pagar.addActionListener(e -> pagar());
        cancelar.addActionListener(e -> cancelar());
        aplicarPromo.addActionListener(e -> aplicarPromocion());
    }

    // ------------------------------------------------------------------ armado de la pantalla

    private JComponent columnaMapa() {
        JPanel filtros = new JPanel(new GridLayout(2, 4, 12, 4));
        filtros.setOpaque(false);
        filtros.add(Estilo.etiqueta("Función"));
        filtros.add(Estilo.etiqueta("Sector"));
        filtros.add(Estilo.etiqueta("Cliente"));
        filtros.add(Estilo.etiqueta("Código de preventa"));
        for (JComboBox<?> combo : new JComboBox<?>[]{funciones, sectores, clientes}) {
            combo.setPreferredSize(new Dimension(160, 36));
            combo.setBackground(Color.WHITE);
        }
        funciones.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> lista, Object valor,
                                                                   int indice, boolean sel, boolean foco) {
                super.getListCellRendererComponent(lista, valor, indice, sel, foco);
                if (valor instanceof Funcion f) {
                    setText(f.getFechaHora().format(FECHA) + " · " + f.getEvento().getNombre());
                }
                return this;
            }
        });
        filtros.add(funciones);
        filtros.add(sectores);
        filtros.add(clientes);
        filtros.add(codigoPreventa);

        JPanel arriba = new JPanel(new BorderLayout(0, 4));
        arriba.setOpaque(false);
        arriba.add(filtros, BorderLayout.CENTER);
        arriba.add(avisoPreventa, BorderLayout.SOUTH);

        mapa.setOpaque(false);
        JPanel contenedorMapa = new JPanel(new BorderLayout(0, 14));
        contenedorMapa.setBackground(Color.WHITE);
        contenedorMapa.add(new Escenario(), BorderLayout.NORTH);
        JPanel envoltorio = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        envoltorio.setBackground(Color.WHITE);
        envoltorio.add(mapa);
        contenedorMapa.add(envoltorio, BorderLayout.CENTER);
        contenedorMapa.add(leyenda(), BorderLayout.SOUTH);
        contenedorMapa.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JScrollPane scroll = Estilo.scroll(contenedorMapa);

        JPanel columna = new JPanel(new BorderLayout(0, 14));
        columna.add(arriba, BorderLayout.NORTH);
        columna.add(scroll, BorderLayout.CENTER);
        return Estilo.tarjeta(columna);
    }

    private JComponent columnaResumen() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(PanelAltaSede.alinear(Estilo.seccion("Tu compra")));
        panel.add(Box.createVerticalStrut(8));
        panel.add(PanelAltaSede.alinear(detalle));
        panel.add(Box.createVerticalStrut(2));
        panel.add(PanelAltaSede.alinear(precioUnitario));
        panel.add(Box.createVerticalStrut(12));

        paqueteVip.setOpaque(false);
        paqueteVip.setFont(Estilo.CUERPO);
        panel.add(PanelAltaSede.alinear(paqueteVip));
        panel.add(Box.createVerticalStrut(10));

        panel.add(PanelAltaSede.alinear(Estilo.etiqueta("Código promocional")));
        panel.add(Box.createVerticalStrut(4));
        JPanel promo = new JPanel(new BorderLayout(8, 0));
        promo.setOpaque(false);
        promo.add(codigoPromo, BorderLayout.CENTER);
        promo.add(aplicarPromo, BorderLayout.EAST);
        promo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        panel.add(PanelAltaSede.alinear(promo));
        panel.add(Box.createVerticalStrut(16));

        panel.add(PanelAltaSede.alinear(fila("Subtotal", subtotal)));
        panel.add(PanelAltaSede.alinear(fila("Promoción", descuento)));
        panel.add(PanelAltaSede.alinear(fila("Cargo por servicio", cargos)));
        total.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        panel.add(PanelAltaSede.alinear(fila("Total", total)));
        panel.add(Box.createVerticalStrut(14));

        reloj.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        reloj.setForeground(Estilo.ACENTO);
        panel.add(PanelAltaSede.alinear(reloj));
        panel.add(Box.createVerticalStrut(10));

        panel.add(PanelAltaSede.alinear(Estilo.etiqueta("Medio de pago")));
        panel.add(Box.createVerticalStrut(4));
        medioPago.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        medioPago.setBackground(Color.WHITE);
        panel.add(PanelAltaSede.alinear(medioPago));
        panel.add(Box.createVerticalStrut(14));

        JPanel botones = new JPanel(new GridLayout(1, 2, 8, 0));
        botones.setOpaque(false);
        botones.add(cancelar);
        botones.add(reservar);
        botones.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        panel.add(PanelAltaSede.alinear(botones));
        panel.add(Box.createVerticalStrut(8));
        pagar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        panel.add(PanelAltaSede.alinear(pagar));
        panel.add(Box.createVerticalGlue());

        JLabel adelantar = Estilo.subtitulo("<html><u>Demo: adelantar el reloj 5 minutos</u></html>");
        adelantar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        adelantar.setToolTipText("Simula que pasaron 5 minutos para mostrar cómo vence la reserva");
        adelantar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                desfaseReloj = desfaseReloj.plusMinutes(5);
                tic();
            }
        });
        panel.add(PanelAltaSede.alinear(adelantar));
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

    private JComponent leyenda() {
        JPanel leyenda = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 0));
        leyenda.setBackground(Color.WHITE);
        leyenda.add(muestra(LIBRE, "Libre"));
        leyenda.add(muestra(Estilo.ACENTO, "Seleccionado"));
        leyenda.add(muestra(MIO, "Tu reserva"));
        leyenda.add(muestra(OCUPADO, "Ocupado"));
        return leyenda;
    }

    private static JComponent muestra(Color color, String texto) {
        JLabel label = Estilo.subtitulo(texto);
        label.setIcon(new javax.swing.Icon() {
            public void paintIcon(java.awt.Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillRoundRect(x, y, 14, 14, 5, 5);
                g2.setColor(Estilo.BORDE.darker());
                g2.drawRoundRect(x, y, 14, 14, 5, 5);
                g2.dispose();
            }

            public int getIconWidth() {
                return 16;
            }

            public int getIconHeight() {
                return 16;
            }
        });
        return label;
    }

    // ------------------------------------------------------------------ datos

    @Override
    public void alMostrar() {
        if (compra != null) {
            dibujarMapa();
            return;
        }
        cargando = true;
        Object funcionAnterior = funciones.getSelectedItem();
        funciones.removeAllItems();
        boleteria.buscarFunciones("").forEach(funciones::addItem);
        if (funcionAnterior != null) {
            funciones.setSelectedItem(funcionAnterior);
        }
        clientes.removeAllItems();
        boleteria.getClientes().forEach(clientes::addItem);
        cargando = false;
        PaqueteVIP vip = paquete();
        paqueteVip.setText(vip == null ? "Sin paquetes VIP"
                : "Sumar " + vip.getNombre() + " (" + Estilo.pesos(vip.getPrecio()) + ")");
        paqueteVip.setEnabled(vip != null);
        cargarSectores();
    }

    private PaqueteVIP paquete() {
        return boleteria.getPaquetes().isEmpty() ? null : boleteria.getPaquetes().get(0);
    }

    private Funcion funcion() {
        return (Funcion) funciones.getSelectedItem();
    }

    private Sector sector() {
        return (Sector) sectores.getSelectedItem();
    }

    private LocalDateTime ahora() {
        return LocalDateTime.now().plus(desfaseReloj);
    }

    private void cargarSectores() {
        cargando = true;
        sectores.removeAllItems();
        Funcion funcion = funcion();
        if (funcion != null) {
            funcion.getSede().getSectores().forEach(sectores::addItem);
            avisoPreventa.setText(funcion.estaEnPreventa(ahora())
                    ? "Esta función está en preventa: necesitás el código de acceso." : " ");
            avisoPreventa.setForeground(Estilo.AVISO);
        }
        cargando = false;
        seleccion.clear();
        dibujarMapa();
    }

    // ------------------------------------------------------------------ mapa de asientos

    private void dibujarMapa() {
        mapa.removeAll();
        Funcion funcion = funcion();
        Sector sector = sector();
        if (funcion != null && sector != null) {
            boleteria.liberarReservasVencidas(ahora());
            if (sector instanceof SectorNumerado numerado) {
                dibujarAsientos(funcion, numerado);
            } else {
                dibujarGeneral(funcion, (SectorGeneral) sector);
            }
        }
        mapa.revalidate();
        mapa.repaint();
        actualizarResumen();
    }

    private void dibujarAsientos(Funcion funcion, SectorNumerado sector) {
        int fila = 0;
        for (String nombreFila : sector.getFilas()) {
            GridBagConstraints c = new GridBagConstraints();
            c.gridy = fila;
            c.gridx = 0;
            c.insets = new Insets(2, 0, 2, 8);
            JLabel etiqueta = Estilo.etiqueta(nombreFila);
            mapa.add(etiqueta, c);
            int columna = 1;
            for (Asiento asiento : sector.getAsientosDeFila(nombreFila)) {
                c = new GridBagConstraints();
                c.gridy = fila;
                c.gridx = columna++;
                c.insets = new Insets(2, 2, 2, 2);
                mapa.add(new BotonAsiento(asiento, estado(funcion, sector, asiento)), c);
            }
            fila++;
        }
    }

    private void dibujarGeneral(Funcion funcion, SectorGeneral sector) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        JLabel titulo = Estilo.seccion(sector.getNombre() + " · sector general (de pie)");
        JLabel libres = Estilo.subtitulo(Estilo.numero(funcion.lugaresLibres(sector)) + " de "
                + Estilo.numero(sector.getCapacidad()) + " lugares libres · sin asiento asignado");
        JPanel elegir = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        elegir.setOpaque(false);
        elegir.add(Estilo.etiqueta("Cantidad de entradas"));
        cantidad.setPreferredSize(new Dimension(80, 34));
        cantidad.setEnabled(compra == null);
        elegir.add(cantidad);
        panel.add(PanelAltaSede.alinear(titulo));
        panel.add(Box.createVerticalStrut(6));
        panel.add(PanelAltaSede.alinear(libres));
        panel.add(Box.createVerticalStrut(14));
        panel.add(PanelAltaSede.alinear(elegir));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
        mapa.add(panel);
    }

    private Color estado(Funcion funcion, Sector sector, Asiento asiento) {
        if (compra != null && compra.getEntradasActivas().stream()
                .anyMatch(e -> e.getSector().equals(sector) && asiento.equals(e.getAsiento())
                        && e.getFuncion() == funcion)) {
            return MIO;
        }
        if (seleccion.contains(asiento)) {
            return Estilo.ACENTO;
        }
        return funcion.estaDisponible(sector, asiento) ? LIBRE : OCUPADO;
    }

    /** Un asiento del mapa: se marca y desmarca con un click mientras no haya una reserva en curso. */
    private final class BotonAsiento extends JComponent {

        private final Asiento asiento;
        private final Color color;

        BotonAsiento(Asiento asiento, Color color) {
            this.asiento = asiento;
            this.color = color;
            setPreferredSize(new Dimension(26, 26));
            setToolTipText("Fila " + asiento.getFila() + " · Asiento " + asiento.getNumero());
            boolean clickeable = compra == null && (color == LIBRE || color == Estilo.ACENTO);
            if (clickeable) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (!seleccion.remove(asiento)) {
                            if (seleccion.size() >= 10) {
                                aviso.error("Podés elegir hasta 10 asientos por compra.");
                                return;
                            }
                            seleccion.add(asiento);
                        }
                        aviso.ocultar();
                        dibujarMapa();
                    }
                });
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 8, 8);
            g2.setColor(color == LIBRE ? Estilo.BORDE.darker() : color.darker());
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 8, 8);
            g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
            g2.setColor(color == LIBRE || color == OCUPADO ? Estilo.TEXTO_SUAVE : Color.WHITE);
            String numero = String.valueOf(asiento.getNumero());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(numero, (getWidth() - fm.stringWidth(numero)) / 2,
                    (getHeight() + fm.getAscent()) / 2 - 2);
            g2.dispose();
        }
    }

    /** El escenario (la "pantalla") arriba del mapa, para orientarse. */
    private static final class Escenario extends JComponent {

        Escenario() {
            setPreferredSize(new Dimension(0, 46));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int ancho = Math.min(560, getWidth() - 40);
            int x = (getWidth() - ancho) / 2;
            g2.setColor(Estilo.LATERAL);
            g2.fillRoundRect(x, 6, ancho, 30, 30, 30);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
            String texto = "ESCENARIO / PANTALLA";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(texto, (getWidth() - fm.stringWidth(texto)) / 2, 26);
            g2.dispose();
        }
    }

    // ------------------------------------------------------------------ resumen y acciones

    private void actualizarResumen() {
        Funcion funcion = funcion();
        Sector sector = sector();
        boolean enCurso = compra != null;
        funciones.setEnabled(!enCurso);
        sectores.setEnabled(!enCurso);
        clientes.setEnabled(!enCurso);
        codigoPreventa.setEnabled(!enCurso);
        paqueteVip.setEnabled(!enCurso && paquete() != null);
        reservar.setEnabled(!enCurso);
        pagar.setEnabled(enCurso);
        codigoPromo.setEnabled(enCurso);
        aplicarPromo.setEnabled(enCurso);

        if (funcion == null || sector == null) {
            detalle.setText("No hay funciones disponibles.");
            return;
        }
        double unitario = funcion.calcularPrecio(sector);
        precioUnitario.setText(sector.getNombre() + " · " + Estilo.pesos(unitario) + " por entrada");

        if (enCurso) {
            StringBuilder texto = new StringBuilder("<html>Reservado: ");
            for (Entrada e : compra.getEntradasActivas()) {
                texto.append(e.getAsiento() == null ? "general " : e.getAsiento() + " ");
            }
            detalle.setText(texto.append("</html>").toString());
            subtotal.setText(Estilo.pesos(compra.calcularSubtotal()));
            descuento.setText(compra.calcularDescuento() > 0 ? "- " + Estilo.pesos(compra.calcularDescuento()) : "—");
            cargos.setText(Estilo.pesos(compra.calcularCargos()));
            total.setText(Estilo.pesos(compra.calcularTotal()));
            return;
        }
        int entradas = sector instanceof SectorNumerado ? seleccion.size() : (Integer) cantidad.getValue();
        if (sector instanceof SectorNumerado) {
            detalle.setText(seleccion.isEmpty() ? "Elegí tus asientos en el mapa."
                    : "<html>Elegidos: " + String.join(" ", seleccion.stream().map(Asiento::toString).toList())
                    + "</html>");
        } else {
            detalle.setText(entradas + " entrada(s) en " + sector.getNombre());
        }
        double estimado = entradas * unitario + (paqueteVip.isSelected() && paquete() != null
                ? paquete().getPrecio() : 0);
        subtotal.setText(entradas == 0 ? "—" : Estilo.pesos(estimado));
        descuento.setText("—");
        cargos.setText(entradas == 0 ? "—" : "≈ " + Estilo.pesos(Math.round(estimado * 0.12)));
        total.setText(entradas == 0 ? "—" : "≈ " + Estilo.pesos(estimado + Math.round(estimado * 0.12)));
        reloj.setText(" ");
    }

    private void reservar() {
        Funcion funcion = funcion();
        Sector sector = sector();
        Cliente cliente = (Cliente) clientes.getSelectedItem();
        if (funcion == null || sector == null || cliente == null) {
            aviso.error("Elegí función, sector y cliente.");
            return;
        }
        Compra nueva = boleteria.iniciarCompra(cliente, codigoPreventa.getText(), ahora());
        try {
            if (sector instanceof SectorNumerado numerado) {
                boleteria.reservarAsientos(nueva, funcion, numerado, new ArrayList<>(seleccion), ahora());
            } else {
                boleteria.reservarAsientos(nueva, funcion, (SectorGeneral) sector, (Integer) cantidad.getValue(),
                        ahora());
            }
            if (paqueteVip.isSelected() && paquete() != null) {
                boleteria.agregarPaquete(nueva, paquete());
            }
            compra = nueva;
            seleccion.clear();
            timer.start();
            aviso.info("¡Listo! Tus lugares quedan reservados por " + Compra.MINUTOS_RESERVA
                    + " minutos. Elegí el medio de pago y pagá.");
        } catch (SeleccionInvalidaException | IllegalStateException ex) {
            boleteria.cancelarCompra(nueva);
            aviso.error(ex.getMessage());
        }
        dibujarMapa();
        tic();
    }

    private void aplicarPromocion() {
        try {
            boleteria.aplicarCodigoPromocional(compra, codigoPromo.getText());
            aviso.exito("Promoción aplicada: " + compra.getPromocion().getDescripcion());
        } catch (IllegalArgumentException ex) {
            aviso.error(ex.getMessage());
        }
        actualizarResumen();
    }

    private void pagar() {
        try {
            Factura factura = boleteria.confirmarCompra(compra, (MedioPago) medioPago.getSelectedItem(), ahora());
            terminarCompra();
            aviso.exito("Compra confirmada. Comprobante N° " + factura.getNumero() + ".");
            SwingUtilities.invokeLater(() ->
                    new FacturaDialog(SwingUtilities.getWindowAncestor(this), factura).setVisible(true));
        } catch (ReservaVencidaException ex) {
            terminarCompra();
            aviso.error(ex.getMessage());
        }
    }

    private void cancelar() {
        if (compra != null) {
            boleteria.cancelarCompra(compra);
            aviso.info("Reserva cancelada: los asientos volvieron a estar libres.");
        } else {
            aviso.ocultar();
        }
        terminarCompra();
    }

    private void terminarCompra() {
        compra = null;
        timer.stop();
        seleccion.clear();
        codigoPromo.setText("");
        reloj.setText(" ");
        dibujarMapa();
    }

    /** Cada segundo actualiza la cuenta regresiva; si llega a cero, la reserva vence y se liberan los asientos. */
    private void tic() {
        if (compra == null) {
            return;
        }
        if (compra.estaVencida(ahora())) {
            boleteria.liberarReservasVencidas(ahora());
            terminarCompra();
            aviso.error("Se terminaron los " + Compra.MINUTOS_RESERVA
                    + " minutos: la reserva venció y los asientos se liberaron.");
            return;
        }
        long segundos = compra.getSegundosRestantes(ahora());
        reloj.setText(String.format("%02d:%02d  para pagar", segundos / 60, segundos % 60));
    }
}
