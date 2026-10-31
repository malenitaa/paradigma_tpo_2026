package ar.edu.uade.poo.grupo3.tpo.modelo.ventas;

/** Cargo por servicio o impuesto, expresado como porcentaje. */
public class Cargo {

    private final String nombre;
    private final double porcentaje;

    public Cargo(String nombre, double porcentaje) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del cargo es obligatorio");
        }
        if (porcentaje < 0 || porcentaje > 100) {
            throw new IllegalArgumentException("El porcentaje debe estar entre 0 y 100");
        }
        this.nombre = nombre.trim();
        this.porcentaje = porcentaje;
    }

    public double aplicar(double monto) {
        return Math.round(monto * porcentaje / 100.0);
    }

    public String getNombre() {
        return nombre;
    }

    public double getPorcentaje() {
        return porcentaje;
    }

    @Override
    public String toString() {
        return nombre + " (" + porcentaje + "%)";
    }
}
