package ar.edu.uade.poo.grupo3.tpo.ventas;

import java.time.LocalDateTime;

/** Devolución de una entrada: se reintegra el precio de la entrada, sin los cargos. */
public class Devolucion {

    public static final int HORAS_LIMITE = 48;

    private final Entrada entrada;
    private final String motivo;
    private final LocalDateTime fecha;
    private final double montoReintegro;

    public Devolucion(Entrada entrada, String motivo, LocalDateTime fecha) {
        if (entrada == null) {
            throw new IllegalArgumentException("La entrada es obligatoria");
        }
        this.entrada = entrada;
        this.motivo = (motivo == null || motivo.isBlank()) ? "Sin motivo" : motivo.trim();
        this.fecha = fecha;
        this.montoReintegro = calcularReintegro();
    }

    public final double calcularReintegro() {
        return entrada.getPrecio();
    }

    public Entrada getEntrada() {
        return entrada;
    }

    public String getMotivo() {
        return motivo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public double getMontoReintegro() {
        return montoReintegro;
    }
}
