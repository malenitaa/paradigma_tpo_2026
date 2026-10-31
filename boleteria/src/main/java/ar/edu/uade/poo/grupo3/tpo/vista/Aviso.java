package ar.edu.uade.poo.grupo3.tpo.vista;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** Mensaje en línea (éxito, error o información) que se muestra dentro de la pantalla. */
public class Aviso extends JLabel {

    private Color fondo = Estilo.FONDO;

    public Aviso() {
        setFont(Estilo.CUERPO);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));
        setVisible(false);
    }

    public void exito(String mensaje) {
        mostrar("✓  " + mensaje, Estilo.EXITO, Estilo.EXITO_SUAVE);
    }

    public void error(String mensaje) {
        mostrar("✕  " + mensaje, Estilo.PELIGRO, Estilo.PELIGRO_SUAVE);
    }

    public void info(String mensaje) {
        mostrar("i  " + mensaje, Estilo.AVISO, Estilo.AVISO_SUAVE);
    }

    public void ocultar() {
        setVisible(false);
    }

    private void mostrar(String mensaje, Color texto, Color fondo) {
        this.fondo = fondo;
        setForeground(texto);
        setText("<html>" + mensaje.replace("<", "&lt;") + "</html>");
        setVisible(true);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(fondo);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
        g2.dispose();
        super.paintComponent(g);
    }
}
