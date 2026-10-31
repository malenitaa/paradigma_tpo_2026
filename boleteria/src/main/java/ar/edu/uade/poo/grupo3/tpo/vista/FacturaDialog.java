package ar.edu.uade.poo.grupo3.tpo.vista;

import ar.edu.uade.poo.grupo3.tpo.modelo.ventas.Factura;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Window;

/** Muestra el comprobante emitido, con estética de ticket. */
public class FacturaDialog extends JDialog {

    public FacturaDialog(Window padre, Factura factura) {
        super(padre, "Comprobante " + factura.getNumero(), ModalityType.APPLICATION_MODAL);
        setSize(560, 700);
        setLocationRelativeTo(padre);

        JPanel ticket = new JPanel(new BorderLayout());
        ticket.setBackground(Color.WHITE);
        ticket.setBorder(BorderFactory.createLineBorder(Estilo.BORDE));

        JPanel cabecera = new JPanel(new BorderLayout(0, 2));
        cabecera.setBackground(Estilo.LATERAL);
        cabecera.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        JLabel marca = new JLabel("● Boletería");
        marca.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        marca.setForeground(Color.WHITE);
        JLabel numero = new JLabel("¡Compra confirmada! · Comprobante N° " + factura.getNumero());
        numero.setForeground(Estilo.LATERAL_TEXTO);
        cabecera.add(marca, BorderLayout.NORTH);
        cabecera.add(numero, BorderLayout.CENTER);
        ticket.add(cabecera, BorderLayout.NORTH);

        JTextArea texto = new JTextArea(factura.generarTexto());
        texto.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        texto.setEditable(false);
        texto.setBorder(BorderFactory.createEmptyBorder(16, 20, 8, 20));
        JScrollPane scroll = new JScrollPane(texto);
        scroll.setBorder(null);
        ticket.add(scroll, BorderLayout.CENTER);

        JPanel pie = new JPanel(new BorderLayout(0, 6));
        pie.setBackground(Color.WHITE);
        pie.setBorder(BorderFactory.createEmptyBorder(8, 20, 16, 20));
        pie.add(new CodigoDeBarras(factura.getNumero()), BorderLayout.CENTER);
        JLabel guardado = Estilo.subtitulo("Guardado en datos/facturas/FAC-" + factura.getNumero() + ".txt");
        pie.add(guardado, BorderLayout.SOUTH);
        ticket.add(pie, BorderLayout.SOUTH);

        JButton cerrar = Estilo.boton("Listo", Estilo.TipoBoton.PRIMARIO);
        cerrar.addActionListener(e -> dispose());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        botones.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        botones.add(cerrar);

        JPanel fondo = new JPanel(new BorderLayout());
        fondo.setBackground(Estilo.FONDO);
        fondo.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        fondo.add(ticket, BorderLayout.CENTER);
        fondo.add(botones, BorderLayout.SOUTH);
        setContentPane(fondo);
    }

    /** Código de barras decorativo generado a partir del número de comprobante. */
    private static final class CodigoDeBarras extends JComponent {

        private final String valor;

        CodigoDeBarras(String valor) {
            this.valor = valor;
            setPreferredSize(new Dimension(0, 54));
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(Estilo.TEXTO);
            int x = 0;
            long semilla = valor.hashCode() * 31L + 7;
            while (x < getWidth()) {
                semilla = (semilla * 1103515245L + 12345L) & 0x7fffffffL;
                int ancho = 1 + (int) (semilla % 3);
                if ((semilla >> 4) % 3 != 0) {
                    g.fillRect(x, 0, ancho, getHeight() - 4);
                }
                x += ancho + 1;
            }
        }
    }
}
