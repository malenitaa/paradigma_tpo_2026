package ar.edu.uade.poo.grupo3.tpo.modelo.eventos;

import ar.edu.uade.poo.grupo3.tpo.modelo.enums.EstadoFuncion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Evento {

    protected final int id;
    protected String nombre;
    protected String descripcion;
    protected int duracionMinutos;
    protected final List<Funcion> funciones;

    protected Evento(int id, String nombre, String descripcion, int duracionMinutos) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del evento es obligatorio");
        }
        if (duracionMinutos <= 0) {
            throw new IllegalArgumentException("La duración debe ser positiva");
        }
        this.id = id;
        this.nombre = nombre.trim();
        this.descripcion = descripcion == null ? "" : descripcion.trim();
        this.duracionMinutos = duracionMinutos;
        this.funciones = new ArrayList<>();
    }

    public void agregarFuncion(Funcion funcion) {
        if (funcion == null) {
            throw new IllegalArgumentException("La función no puede ser nula");
        }
        funciones.add(funcion);
    }

    public List<Funcion> getFuncionesDisponibles() {
        return funciones.stream().filter(f -> f.getEstado() == EstadoFuncion.PROGRAMADA).toList();
    }

    public List<Funcion> getFunciones() {
        return Collections.unmodifiableList(funciones);
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    public abstract String getTipo();

    @Override
    public String toString() {
        return nombre;
    }
}
