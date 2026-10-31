package ar.edu.uade.poo.grupo3.tpo.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.Locale;

/** Paleta, tipografías y componentes reutilizables para que todas las pantallas se vean iguales. */
public final class Estilo {

    public static final Color FONDO = new Color(0xF4F5F8);
    public static final Color SUPERFICIE = Color.WHITE;
    public static final Color BORDE = new Color(0xE3E5EB);
    public static final Color TEXTO = new Color(0x171923);
    public static final Color TEXTO_SUAVE = new Color(0x6B7080);
    public static final Color ACENTO = new Color(0x5B3DF5);
    public static final Color ACENTO_HOVER = new Color(0x4A2CE0);
    public static final Color ACENTO_SUAVE = new Color(0xEFEBFF);
    public static final Color LATERAL = new Color(0x13121B);
    public static final Color LATERAL_ACTIVO = new Color(0x26243A);
    public static final Color LATERAL_TEXTO = new Color(0xB4B2C8);
    public static final Color PELIGRO = new Color(0xD63B3B);
    public static final Color PELIGRO_SUAVE = new Color(0xFDECEC);
    public static final Color EXITO = new Color(0x15803D);
    public static final Color EXITO_SUAVE = new Color(0xE7F6EC);
    public static final Color AVISO = new Color(0xA16207);
    public static final Color AVISO_SUAVE = new Color(0xFEF6E0);

    public static final Font TITULO = new Font(Font.SANS_SERIF, Font.BOLD, 24);
    public static final Font SUBTITULO = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    public static final Font SECCION = new Font(Font.SANS_SERIF, Font.BOLD, 15);
    public static final Font CUERPO = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    public static final Font ETIQUETA = new Font(Font.SANS_SERIF, Font.BOLD, 12);
    public static final Font NUMERO_GRANDE = new Font(Font.SANS_SERIF, Font.BOLD, 28);

    private static final NumberFormat NUMEROS = NumberFormat.getIntegerInstance(new Locale("es", "AR"));

    public enum TipoBoton { PRIMARIO, SECUNDARIO, PELIGRO }

    private Estilo() {
    }

    /** Ajustes globales de Swing: look & feel multiplataforma para que se vea igual en Mac y Windows. */
    public static void aplicarGlobal() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignorada) {
            // si falla, Swing usa el look & feel por defecto
        }
        UIManager.put("Panel.background", FONDO);
        UIManager.put("Label.font", CUERPO);
        UIManager.put("Label.foreground", TEXTO);
        UIManager.put("TextField.font", CUERPO);
        UIManager.put("ComboBox.font", CUERPO);
        UIManager.put("ComboBox.background", SUPERFICIE);
        UIManager.put("Spinner.font", CUERPO);
        UIManager.put("Table.font", CUERPO);
        UIManager.put("OptionPane.messageFont", CUERPO);
        UIManager.put("OptionPane.buttonFont", ETIQUETA);
        UIManager.put("ToolTip.font", CUERPO);
        UIManager.put("ScrollBar.width", 10);
    }

    public static String numero(long valor) {
        return NUMEROS.format(valor);
    }

    public static String pesos(double valor) {
        return "$ " + NUMEROS.format(Math.round(valor));
    }

    // ------------------------------------------------------------------ textos

    public static JLabel titulo(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(TITULO);
        label.setForeground(TEXTO);
        return label;
    }

    public static JLabel subtitulo(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(SUBTITULO);
        label.setForeground(TEXTO_SUAVE);
        return label;
    }

    public static JLabel seccion(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(SECCION);
        label.setForeground(TEXTO);
        return label;
    }

    public static JLabel etiqueta(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(ETIQUETA);
        label.setForeground(TEXTO_SUAVE);
        return label;
    }

    /** Encabezado de cada pantalla: título grande y una línea que explica qué se hace ahí. */
    public static JPanel encabezado(String titulo, String subtitulo) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);
        panel.add(titulo(titulo), BorderLayout.NORTH);
        panel.add(subtitulo(subtitulo), BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));
        return panel;
    }

    // ------------------------------------------------------------------ contenedores

    /** Tarjeta blanca con borde suave y margen interno. */
    public static JPanel tarjeta(JComponent contenido) {
        JPanel tarjeta = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SUPERFICIE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.setColor(BORDE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
            }
        };
        tarjeta.setOpaque(false);
        tarjeta.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        contenido.setOpaque(false);
        tarjeta.add(contenido, BorderLayout.CENTER);
        return tarjeta;
    }

    public static JTextField campo() {
        JTextField campo = new JTextField();
        campo.setFont(CUERPO);
        campo.setBorder(bordeCampo());
        campo.setPreferredSize(new Dimension(220, 36));
        return campo;
    }

    public static Border bordeCampo() {
        return BorderFactory.createCompoundBorder(new BordeRedondeado(BORDE, 8),
                BorderFactory.createEmptyBorder(6, 10, 6, 10));
    }

    // ------------------------------------------------------------------ botones

    public static JButton boton(String texto, TipoBoton tipo) {
        return new Boton(texto, tipo);
    }

    private static final class Boton extends JButton {

        private final TipoBoton tipo;
        private boolean encima;

        Boton(String texto, TipoBoton tipo) {
            super(texto);
            this.tipo = tipo;
            setFont(ETIQUETA);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
            setForeground(tipo == TipoBoton.SECUNDARIO ? TEXTO : Color.WHITE);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    encima = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    encima = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fondo = switch (tipo) {
                case PRIMARIO -> encima ? ACENTO_HOVER : ACENTO;
                case PELIGRO -> encima ? PELIGRO.darker() : PELIGRO;
                case SECUNDARIO -> encima ? new Color(0xF0F1F5) : SUPERFICIE;
            };
            if (!isEnabled()) {
                fondo = new Color(0xD5D7DE);
            }
            g2.setColor(fondo);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            if (tipo == TipoBoton.SECUNDARIO) {
                g2.setColor(BORDE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ------------------------------------------------------------------ tablas

    public static DefaultTableModel modeloTabla(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    public static JTable tabla(DefaultTableModel modelo) {
        JTable tabla = new JTable(modelo);
        tabla.setFont(CUERPO);
        tabla.setRowHeight(34);
        tabla.setShowVerticalLines(false);
        tabla.setGridColor(BORDE);
        tabla.setIntercellSpacing(new Dimension(0, 1));
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setSelectionBackground(ACENTO_SUAVE);
        tabla.setSelectionForeground(TEXTO);
        tabla.setFillsViewportHeight(true);
        tabla.setBackground(SUPERFICIE);
        tabla.setAutoCreateRowSorter(true);
        DefaultTableCellRenderer celdas = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor, boolean seleccionada,
                                                           boolean foco, int fila, int columna) {
                super.getTableCellRendererComponent(t, valor, seleccionada, false, fila, columna);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                boolean numerico = columna > 0 && valor instanceof Number
                        || (columna > 0 && valor instanceof String texto && texto.matches("[$×\\d.,/ ]+") && !texto.isBlank());
                setHorizontalAlignment(numerico ? RIGHT : LEFT);
                return this;
            }
        };
        tabla.setDefaultRenderer(Object.class, celdas);
        tabla.setDefaultRenderer(Number.class, celdas);
        tabla.setDefaultRenderer(Integer.class, celdas);
        tabla.setDefaultRenderer(Double.class, celdas);
        JTableHeader header = tabla.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 36));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor, boolean seleccionada,
                                                           boolean foco, int fila, int columna) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(t, valor, false, false, fila, columna);
                label.setFont(ETIQUETA);
                label.setForeground(TEXTO_SUAVE);
                label.setBackground(new Color(0xF8F9FB));
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE),
                        BorderFactory.createEmptyBorder(0, 12, 0, 12)));
                return label;
            }
        });
        return tabla;
    }

    /** Anchos preferidos de las columnas (en píxeles), en el orden de las columnas. */
    public static void anchos(JTable tabla, int... anchos) {
        for (int i = 0; i < anchos.length && i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
    }

    public static JScrollPane scroll(JComponent componente) {
        JScrollPane scroll = new JScrollPane(componente);
        scroll.setBorder(new BordeRedondeado(BORDE, 10));
        scroll.getViewport().setBackground(SUPERFICIE);
        return scroll;
    }

    /** Borde con esquinas redondeadas. */
    public static final class BordeRedondeado implements Border {

        private final Color color;
        private final int radio;

        public BordeRedondeado(Color color, int radio) {
            this.color = color;
            this.radio = radio;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int ancho, int alto) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(x, y, ancho - 1, alto - 1, radio, radio);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(1, 1, 1, 1);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }
}
