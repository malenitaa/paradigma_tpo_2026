package ar.edu.uade.poo.grupo3.tpo.ventas;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Comprobante que se emite al confirmar una compra. Se guarda como .txt en datos/facturas. */
public class Factura {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static int secuencia = 1;

    private final String numero;
    private final LocalDateTime fecha;
    private final Compra compra;

    public Factura(Compra compra, LocalDateTime fecha) {
        if (compra == null || compra.getPago() == null) {
            throw new IllegalArgumentException("Solo se factura una compra pagada");
        }
        this.numero = String.format("0001-%08d", secuencia++);
        this.fecha = fecha;
        this.compra = compra;
    }

    public String generarTexto() {
        StringBuilder sb = new StringBuilder();
        String linea = "-".repeat(56) + "\n";
        sb.append("BOLETERÍA TPO — GRUPO 3\n");
        sb.append("Comprobante de compra N° ").append(numero).append('\n');
        sb.append("Fecha: ").append(fecha.format(FORMATO)).append('\n');
        sb.append("Cliente: ").append(compra.getCliente().getNombre())
                .append(" · DNI ").append(compra.getCliente().getDni())
                .append(" · ").append(compra.getCliente().getEmail()).append('\n');
        sb.append(linea);
        compra.getEntradasActivas().stream().map(Entrada::getFuncion).distinct().forEach(f -> {
            sb.append(f.getEvento().getNombre()).append('\n');
            sb.append(f.getSede().getNombre()).append(" · ")
                    .append(f.getFechaHora().format(FORMATO)).append(" hs\n\n");
        });
        for (Entrada e : compra.getEntradasActivas()) {
            sb.append(String.format("%-9s %-30s %14s%n", e.getCodigo(), e.getUbicacion(), pesos(e.getPrecio())));
        }
        for (PaqueteVIP p : compra.getPaquetes()) {
            sb.append(String.format("%-40s %14s%n", "Paquete " + p.getNombre(), pesos(p.getPrecio())));
        }
        sb.append(linea);
        sb.append(String.format("%-40s %14s%n", "Subtotal", pesos(compra.calcularSubtotal())));
        if (compra.calcularDescuento() > 0) {
            sb.append(String.format("%-40s %14s%n", "Promoción: " + compra.getPromocion().getDescripcion(),
                    "-" + pesos(compra.calcularDescuento())));
        }
        for (Cargo c : compra.getCargos()) {
            sb.append(String.format("%-40s %14s%n", c.toString(),
                    pesos(c.aplicar(compra.calcularSubtotal() - compra.calcularDescuento()))));
        }
        sb.append(String.format("%-40s %14s%n", "TOTAL", pesos(compra.calcularTotal())));
        sb.append(linea);
        sb.append("Pago: ").append(compra.getPago().getMedio())
                .append(" · Operación ").append(compra.getPago().getNroOperacion()).append('\n');
        sb.append("Devoluciones hasta ").append(Devolucion.HORAS_LIMITE)
                .append(" hs antes de la función\n(no se reintegran los cargos).\n");
        return sb.toString();
    }

    private static String pesos(double monto) {
        return String.format(new Locale("es", "AR"), "$ %,.0f", monto);
    }

    public String getNumero() {
        return numero;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public Compra getCompra() {
        return compra;
    }

    public double getTotal() {
        return compra.calcularTotal();
    }
}
