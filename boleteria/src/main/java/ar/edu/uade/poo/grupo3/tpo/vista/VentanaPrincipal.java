package ar.edu.uade.poo.grupo3.tpo.vista;

import ar.edu.uade.poo.grupo3.tpo.controlador.SedeController;
import ar.edu.uade.poo.grupo3.tpo.controlador.VentaController;
import ar.edu.uade.poo.grupo3.tpo.modelo.Boleteria;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.LinkedHashMap;
import java.util.Map;

/** Ventana principal: barra lateral de navegación y las pantallas en un CardLayout. */
public class VentanaPrincipal extends JFrame {

    private final CardLayout tarjetas = new CardLayout();
    private final JPanel contenido = new JPanel(tarjetas);
    private final Map<String, Pantalla> pantallas = new LinkedHashMap<>();
    private final Map<String, ItemMenu> items = new LinkedHashMap<>();

    public VentanaPrincipal(Boleteria boleteria) {
        super("Boletería · TPO Grupo 3");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1240, 780);
        setMinimumSize(new Dimension(1080, 680));
        setLocationRelativeTo(null);

        SedeController sedes = new SedeController(boleteria);
        VentaController ventas = new VentaController(boleteria);
        registrar("inicio", new PanelInicio(boleteria, () -> ir("alta"), () -> ir("consulta")));
        registrar("consulta", new PanelConsultaSedes(sedes));
        registrar("alta", new PanelAltaSede(sedes));
        registrar("modificacion", new PanelModificacionSede(sedes));
        registrar("baja", new PanelBajaSede(sedes));
        registrar("venta", new PanelVenta(ventas));
        registrar("devolucion", new PanelDevolucion(ventas));
        contenido.setBackground(Estilo.FONDO);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(barraLateral(), BorderLayout.WEST);
        getContentPane().add(contenido, BorderLayout.CENTER);
        ir("inicio");
    }

    private void registrar(String id, Pantalla pantalla) {
        pantallas.put(id, pantalla);
        contenido.add((Component) pantalla, id);
    }

    public void ir(String id) {
        pantallas.get(id).alMostrar();
        tarjetas.show(contenido, id);
        items.forEach((clave, item) -> item.setActivo(clave.equals(id)));
    }

    private JComponent barraLateral() {
        JPanel lateral = new JPanel();
        lateral.setLayout(new BoxLayout(lateral, BoxLayout.Y_AXIS));
        lateral.setBackground(Estilo.LATERAL);
        lateral.setPreferredSize(new Dimension(236, 0));
        lateral.setBorder(BorderFactory.createEmptyBorder(26, 14, 20, 14));

        JLabel marca = new JLabel("● Boletería");
        marca.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        marca.setForeground(Color.WHITE);
        JLabel grupo = new JLabel("Venta de entradas · Grupo 3");
        grupo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        grupo.setForeground(Estilo.LATERAL_TEXTO);
        lateral.add(izquierda(marca));
        lateral.add(Box.createVerticalStrut(4));
        lateral.add(izquierda(grupo));
        lateral.add(Box.createVerticalStrut(28));

        lateral.add(item("inicio", "Inicio"));
        lateral.add(Box.createVerticalStrut(18));
        lateral.add(titulo("GESTIÓN DE SEDES"));
        lateral.add(item("consulta", "Consulta"));
        lateral.add(item("alta", "Alta"));
        lateral.add(item("modificacion", "Modificación"));
        lateral.add(item("baja", "Baja"));
        lateral.add(Box.createVerticalStrut(18));
        lateral.add(titulo("VENTAS"));
        lateral.add(item("venta", "Venta de entradas"));
        lateral.add(item("devolucion","Devoluciones"));
        lateral.add(Box.createVerticalGlue());

        JLabel pie = new JLabel("<html>Datos guardados en<br>la carpeta <b>datos/</b></html>");
        pie.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        pie.setForeground(new Color(0x7D7A93));
        lateral.add(izquierda(pie));
        return lateral;
    }

    private JComponent item(String id, String texto) {
        ItemMenu item = new ItemMenu(texto);
        item.addActionListener(e -> ir(id));
        items.put(id, item);
        return izquierda(item);
    }

    private JComponent proximamente(String texto) {
        ItemMenu item = new ItemMenu(texto);
        item.setEnabled(false);
        item.setToolTipText("Se completa para la entrega final");
        return izquierda(item);
    }

    private JComponent titulo(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        label.setForeground(new Color(0x7D7A93));
        label.setBorder(BorderFactory.createEmptyBorder(0, 12, 6, 0));
        return izquierda(label);
    }

    private static <T extends JComponent> T izquierda(T componente) {
        componente.setAlignmentX(Component.LEFT_ALIGNMENT);
        return componente;
    }

    /** Botón de la barra lateral con resaltado para la pantalla actual. */
    private static final class ItemMenu extends JButton {

        private boolean activo;

        ItemMenu(String texto) {
            super(texto);
            setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            setHorizontalAlignment(LEFT);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setForeground(Estilo.LATERAL_TEXTO);
            setBorder(BorderFactory.createEmptyBorder(9, 12, 9, 12));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        void setActivo(boolean activo) {
            this.activo = activo;
            setForeground(activo ? Color.WHITE : Estilo.LATERAL_TEXTO);
            setFont(getFont().deriveFont(activo ? Font.BOLD : Font.PLAIN));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (activo) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Estilo.LATERAL_ACTIVO);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Estilo.ACENTO);
                g2.fillRoundRect(0, 8, 4, getHeight() - 16, 4, 4);
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }
}
