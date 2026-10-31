package ar.edu.uade.poo.grupo3.tpo.modelo.ventas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Cliente {

    private final String dni;
    private String nombre;
    private final Cuenta cuenta;
    private final List<Compra> compras;

    public Cliente(String dni, String nombre, Cuenta cuenta) {
        if (dni == null || !dni.trim().matches("\\d{7,8}")) {
            throw new IllegalArgumentException("El DNI debe tener 7 u 8 dígitos");
        }
        if (nombre == null || nombre.isBlank() || nombre.contains("|")) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (cuenta == null) {
            throw new IllegalArgumentException("El cliente necesita una cuenta");
        }
        this.dni = dni.trim();
        this.nombre = nombre.trim();
        this.cuenta = cuenta;
        this.compras = new ArrayList<>();
    }

    public void agregarCompra(Compra compra) {
        if (compra != null && !compras.contains(compra)) {
            compras.add(compra);
        }
    }

    public String getDni() {
        return dni;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return cuenta.getEmail();
    }

    public Cuenta getCuenta() {
        return cuenta;
    }

    public List<Compra> getCompras() {
        return Collections.unmodifiableList(compras);
    }

    @Override
    public String toString() {
        return nombre + " (DNI " + dni + ")";
    }
}
