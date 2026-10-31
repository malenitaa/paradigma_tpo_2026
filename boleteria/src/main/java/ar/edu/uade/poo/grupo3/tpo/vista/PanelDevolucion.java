package ar.edu.uade.poo.grupo3.tpo.vista;

import ar.edu.uade.poo.grupo3.tpo.controlador.VentaController;
import ar.edu.uade.poo.grupo3.tpo.modelo.excepcion.DevolucionNoPermitidaException;
import ar.edu.uade.poo.grupo3.tpo.modelo.ventas.Entrada;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Optional;

/**
 * Pantalla para validar entradas y gestionar su devolución.
 */
public class PanelDevolucion extends JPanel implements Pantalla {

    private final VentaController controlador;
    private final JLabel detalle = Estilo.subtitulo("Detalle del reembolso.");
    private final JLabel precioUnitario = Estilo.subtitulo(" ");
    private final JTextField codigoEntrada = new JTextField(10);
    private final JTextArea motivoDevolucion = new JTextArea(4, 20);
    private final JLabel reembolso = new JLabel("—");
    private final JButton pagar = new JButton("Efectuar Devolución");
    private final JButton verificarEntrada = new JButton("Verificar Entrada");
    private final Aviso aviso = new Aviso();

    public PanelDevolucion(VentaController controlador) {
        super(new BorderLayout());
        this.controlador = controlador;
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        add(Estilo.encabezado("Devolución de entradas",
                "Verificá la entrada y consultá el monto a reembolsar antes de confirmar."),
                BorderLayout.NORTH);
        JPanel centro = new JPanel(new BorderLayout(16, 0));
        centro.add(columnaMapa(), BorderLayout.CENTER);
        JPanel resumen = Estilo.tarjeta(columnaResumen());
        resumen.setPreferredSize(new Dimension(330, 0));
        centro.add(resumen, BorderLayout.EAST);
        add(centro, BorderLayout.CENTER);
        add(Estilo.barraInferior(aviso), BorderLayout.SOUTH);

        verificarEntrada.addActionListener(e -> {
            verificarEntrada();
        });

        pagar.addActionListener(e -> {
            efectuarDevolucion();
        });
    }

    // ------------------------------------------------------------------ armado de la pantalla

    private JComponent columnaMapa() {
        JPanel codigo = new JPanel(new BorderLayout(8, 0));
        codigo.add(codigoEntrada, BorderLayout.CENTER);
        codigo.add(verificarEntrada, BorderLayout.EAST);

        JPanel campoCodigo = new JPanel(new BorderLayout(0, 6));
        campoCodigo.add(new JLabel("Código de entrada"), BorderLayout.NORTH);
        campoCodigo.add(codigo, BorderLayout.CENTER);

        motivoDevolucion.setLineWrap(true);
        motivoDevolucion.setWrapStyleWord(true);
        motivoDevolucion.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JScrollPane motivoScroll = new JScrollPane(motivoDevolucion);
        motivoScroll.setBorder(BorderFactory.createLineBorder(Estilo.BORDE));
        motivoScroll.setPreferredSize(new Dimension(0, 180));

        JPanel campoMotivo = new JPanel(new BorderLayout(0, 6));
        campoMotivo.add(new JLabel("Motivo de la devolución"), BorderLayout.NORTH);
        campoMotivo.add(motivoScroll, BorderLayout.CENTER);

        JPanel campos = new JPanel(new BorderLayout(0, 16));
        campos.add(campoCodigo, BorderLayout.NORTH);
        campos.add(campoMotivo, BorderLayout.CENTER);

        JPanel columna = new JPanel(new BorderLayout(0, 16));
        columna.add(Estilo.seccion("Datos de la entrada"), BorderLayout.NORTH);
        columna.add(campos, BorderLayout.CENTER);
        return Estilo.tarjeta(columna);
    }


    private JComponent columnaResumen() {
        reembolso.setFont(Estilo.NUMERO_GRANDE);
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
                aviso.exito("Entrada válida.");
                reembolso.setText(Estilo.pesos(entrada.get().getvalorPagado()));
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
