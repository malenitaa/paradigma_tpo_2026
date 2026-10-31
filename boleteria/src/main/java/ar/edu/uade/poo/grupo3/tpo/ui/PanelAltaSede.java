package ar.edu.uade.poo.grupo3.tpo.ui;

import ar.edu.uade.poo.grupo3.tpo.infraestructura.Sector;
import ar.edu.uade.poo.grupo3.tpo.infraestructura.Sede;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Alta de sede: datos generales + sectores. Al guardar se persiste en datos/sedes.txt y datos/sectores.txt. */
public class PanelAltaSede extends JPanel implements Pantalla {

    private final SedeController controller;
    private final JTextField nombre = Estilo.campo();
    private final JTextField direccion = Estilo.campo();
    private final FormularioSector formulario = new FormularioSector();
    private final DefaultTableModel modeloSectores = FormularioSector.modeloSectores();
    private final JTable tablaSectores = FormularioSector.tablaSectores(modeloSectores);
    private final List<Sector> sectores = new ArrayList<>();
    private final Aviso aviso = new Aviso();

    public PanelAltaSede(SedeController controller) {
        super(new BorderLayout());
        this.controller = controller;
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));
        add(Estilo.encabezado("Alta de sede",
                "Cargá los datos de la sede y armá sus sectores. Necesita al menos un sector para guardarse."),
                BorderLayout.NORTH);

        JPanel datos = new JPanel();
        datos.setLayout(new BoxLayout(datos, BoxLayout.Y_AXIS));
        datos.add(alinear(Estilo.seccion("1. Datos de la sede")));
        datos.add(Box.createVerticalStrut(14));
        datos.add(alinear(Estilo.etiqueta("Nombre")));
        datos.add(Box.createVerticalStrut(4));
        datos.add(alinear(nombre));
        datos.add(Box.createVerticalStrut(12));
        datos.add(alinear(Estilo.etiqueta("Dirección")));
        datos.add(Box.createVerticalStrut(4));
        datos.add(alinear(direccion));
        datos.add(Box.createVerticalGlue());
        nombre.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        direccion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        JPanel tarjetaDatos = Estilo.tarjeta(datos);
        tarjetaDatos.setPreferredSize(new Dimension(300, 0));

        JButton agregar = Estilo.boton("+ Agregar sector", Estilo.TipoBoton.SECUNDARIO);
        agregar.addActionListener(e -> agregarSector());
        JButton quitar = Estilo.boton("Quitar seleccionado", Estilo.TipoBoton.SECUNDARIO);
        quitar.addActionListener(e -> quitarSector());
        JPanel botonesSector = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        botonesSector.setOpaque(false);
        botonesSector.add(agregar);
        botonesSector.add(Box.createHorizontalStrut(8));
        botonesSector.add(quitar);

        JPanel arriba = new JPanel(new BorderLayout(0, 12));
        arriba.setOpaque(false);
        arriba.add(Estilo.seccion("2. Sectores"), BorderLayout.NORTH);
        arriba.add(formulario, BorderLayout.CENTER);
        arriba.add(botonesSector, BorderLayout.SOUTH);
        JPanel sectoresPanel = new JPanel(new BorderLayout(0, 14));
        sectoresPanel.add(arriba, BorderLayout.NORTH);
        sectoresPanel.add(Estilo.scroll(tablaSectores), BorderLayout.CENTER);

        JPanel centro = new JPanel(new BorderLayout(18, 0));
        centro.setOpaque(false);
        centro.add(tarjetaDatos, BorderLayout.WEST);
        centro.add(Estilo.tarjeta(sectoresPanel), BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        JButton limpiar = Estilo.boton("Limpiar", Estilo.TipoBoton.SECUNDARIO);
        limpiar.addActionListener(e -> limpiarTodo());
        JButton guardar = Estilo.boton("Guardar sede", Estilo.TipoBoton.PRIMARIO);
        guardar.addActionListener(e -> guardar());
        add(barraInferior(aviso, limpiar, guardar), BorderLayout.SOUTH);
    }

    private void agregarSector() {
        try {
            Sector sector = formulario.crearSector();
            boolean repetido = sectores.stream().anyMatch(s -> s.getCodigo().equals(sector.getCodigo()));
            if (repetido) {
                throw new IllegalArgumentException("Ya agregaste un sector con código " + sector.getCodigo());
            }
            sectores.add(sector);
            FormularioSector.llenar(modeloSectores, sectores);
            formulario.limpiar();
            aviso.info("Sector " + sector.getCodigo() + " agregado (" + Estilo.numero(sector.getCapacidad())
                    + " lugares). Falta guardar la sede.");
        } catch (IllegalArgumentException ex) {
            aviso.error(ex.getMessage());
        }
    }

    private void quitarSector() {
        int fila = tablaSectores.getSelectedRow();
        if (fila < 0) {
            aviso.error("Seleccioná el sector que querés quitar.");
            return;
        }
        sectores.remove(tablaSectores.convertRowIndexToModel(fila));
        FormularioSector.llenar(modeloSectores, sectores);
        aviso.ocultar();
    }

    private void guardar() {
        try {
            Sede sede = controller.alta(nombre.getText(), direccion.getText(), sectores);
            limpiarTodo();
            aviso.exito("Sede " + sede.getNombre() + " guardada con " + sede.getSectores().size()
                    + " sectores y " + Estilo.numero(sede.getCapacidadTotal()) + " lugares.");
        } catch (IllegalArgumentException ex) {
            aviso.error(ex.getMessage());
        } catch (IOException ex) {
            aviso.error("No se pudo guardar en el archivo: " + ex.getMessage());
        }
    }

    private void limpiarTodo() {
        nombre.setText("");
        direccion.setText("");
        sectores.clear();
        modeloSectores.setRowCount(0);
        formulario.limpiar();
        aviso.ocultar();
    }

    @Override
    public void alMostrar() {
        nombre.requestFocusInWindow();
    }

    static JPanel barraInferior(Aviso aviso, JButton... botones) {
        JPanel barra = new JPanel(new BorderLayout(16, 0));
        barra.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));
        barra.add(aviso, BorderLayout.CENTER);
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        derecha.setOpaque(false);
        for (int i = 0; i < botones.length; i++) {
            if (i > 0) {
                derecha.add(Box.createHorizontalStrut(10));
            }
            derecha.add(botones[i]);
        }
        barra.add(derecha, BorderLayout.EAST);
        return barra;
    }

    static <T extends java.awt.Component> T alinear(T componente) {
        if (componente instanceof javax.swing.JComponent j) {
            j.setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        return componente;
    }
}
