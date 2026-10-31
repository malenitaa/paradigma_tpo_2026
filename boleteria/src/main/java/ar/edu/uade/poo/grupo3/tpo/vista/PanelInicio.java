package ar.edu.uade.poo.grupo3.tpo.vista;

import ar.edu.uade.poo.grupo3.tpo.modelo.Boleteria;
import ar.edu.uade.poo.grupo3.tpo.modelo.eventos.Funcion;

import ar.edu.uade.poo.grupo3.tpo.modelo.infraestructura.Sede;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Resumen del sistema: números principales y próximas funciones con su ocupación. */
public class PanelInicio extends JPanel implements Pantalla {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("EEE dd/MM · HH:mm", new java.util.Locale("es", "AR"));

    private final Boleteria boleteria;
    private final JLabel sedes = numero();
    private final JLabel sectores = numero();
    private final JLabel capacidad = numero();
    private final JLabel cuentas = numero();
    private final DefaultTableModel modeloFunciones =
            Estilo.modeloTabla("Evento", "Sede", "Fecha", "Precio base", "Ocupación", "Estado");

    public PanelInicio(Boleteria boleteria, Runnable irAAlta, Runnable irAConsulta) {
        super(new BorderLayout());
        this.boleteria = boleteria;
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));
        add(Estilo.encabezado("Panel de control",
                "Venta de entradas para sedes de espectáculos · precio según piso y cercanía al escenario"),
                BorderLayout.NORTH);

        JPanel metricas = new JPanel(new GridLayout(1, 4, 16, 0));
        metricas.setOpaque(false);
        metricas.add(metrica("Sedes", sedes, "registradas"));
        metricas.add(metrica("Sectores", sectores, "entre todas las sedes"));
        metricas.add(metrica("Capacidad", capacidad, "lugares en total"));
        metricas.add(metrica("Cuentas", cuentas, "clientes registrados"));

        JPanel funciones = new JPanel(new BorderLayout(0, 12));
        funciones.add(Estilo.seccion("Próximas funciones"), BorderLayout.NORTH);
        var tablaFunciones = Estilo.tabla(modeloFunciones);
        Estilo.anchos(tablaFunciones, 240, 150, 160, 100, 100, 160);
        funciones.add(Estilo.scroll(tablaFunciones), BorderLayout.CENTER);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        acciones.setOpaque(false);
        var nueva = Estilo.boton("+ Nueva sede", Estilo.TipoBoton.PRIMARIO);
        nueva.addActionListener(e -> irAAlta.run());
        var ver = Estilo.boton("Ver sedes", Estilo.TipoBoton.SECUNDARIO);
        ver.addActionListener(e -> irAConsulta.run());
        acciones.add(nueva);
        acciones.add(javax.swing.Box.createHorizontalStrut(10));
        acciones.add(ver);
        funciones.add(acciones, BorderLayout.SOUTH);

        JPanel centro = new JPanel(new BorderLayout(0, 20));
        centro.setOpaque(false);
        centro.add(metricas, BorderLayout.NORTH);
        centro.add(Estilo.tarjeta(funciones), BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);
    }

    @Override
    public void alMostrar() {
        sedes.setText(Estilo.numero(boleteria.getSedes().size()));
        sectores.setText(Estilo.numero(boleteria.getSedes().stream().mapToLong(s -> s.getSectores().size()).sum()));
        capacidad.setText(Estilo.numero(boleteria.getSedes().stream().mapToLong(Sede::getCapacidadTotal).sum()));
        cuentas.setText(Estilo.numero(boleteria.getClientes().size()));
        modeloFunciones.setRowCount(0);
        for (Funcion f : boleteria.buscarFunciones("")) {
            int ocupados = f.getSede().getSectores().stream().mapToInt(f::lugaresOcupados).sum();
            int total = f.getSede().getCapacidadTotal();
            String estado = f.estaEnPreventa(LocalDateTime.now()) ? "EN PREVENTA" : f.getEstado().toString();
            modeloFunciones.addRow(new Object[]{f.getEvento().getNombre(), f.getSede().getNombre(),
                    f.getFechaHora().format(FECHA), Estilo.pesos(f.getPrecioBase()),
                    ocupados + " / " + Estilo.numero(total), estado});
        }
    }

    private static JLabel numero() {
        JLabel label = new JLabel("0");
        label.setFont(Estilo.NUMERO_GRANDE);
        label.setForeground(Estilo.TEXTO);
        return label;
    }

    private static JPanel metrica(String titulo, JLabel valor, String detalle) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.add(Estilo.etiqueta(titulo.toUpperCase()), BorderLayout.NORTH);
        panel.add(valor, BorderLayout.CENTER);
        panel.add(Estilo.subtitulo(detalle), BorderLayout.SOUTH);
        return Estilo.tarjeta(panel);
    }

}
