package ar.edu.uade.poo.grupo3.tpo.modelo.ventas;

import ar.edu.uade.poo.grupo3.tpo.modelo.enums.MedioPago;

import java.time.LocalDateTime;

/** Registro del pago de una compra (no es un cobro real con pasarela). */
public class Pago {

    private final String nroOperacion;
    private final MedioPago medio;
    private final double monto;
    private final LocalDateTime fecha;

    public Pago(String nroOperacion, MedioPago medio, double monto, LocalDateTime fecha) {
        if (medio == null) {
            throw new IllegalArgumentException("Elegí un medio de pago");
        }
        if (monto < 0) {
            throw new IllegalArgumentException("El monto no puede ser negativo");
        }
        this.nroOperacion = nroOperacion;
        this.medio = medio;
        this.monto = monto;
        this.fecha = fecha;
    }

    public String getNroOperacion() {
        return nroOperacion;
    }

    public MedioPago getMedio() {
        return medio;
    }

    public double getMonto() {
        return monto;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}
