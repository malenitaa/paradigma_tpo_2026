package ar.edu.uade.poo.grupo3.tpo.modelo.eventos;

import java.util.Objects;

public class Artista {

    private final String nombre;
    private final String nacionalidad;

    public Artista(String nombre, String nacionalidad) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del artista es obligatorio");
        }
        this.nombre = nombre.trim();
        this.nacionalidad = nacionalidad == null ? "" : nacionalidad.trim();
    }

    public String getNombre() {
        return nombre;
    }

    public String getNacionalidad() {
        return nacionalidad;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Artista otro)) {
            return false;
        }
        return nombre.equalsIgnoreCase(otro.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre.toLowerCase());
    }

    @Override
    public String toString() {
        return nombre;
    }
}
