package ar.edu.uade.poo.grupo3.tpo.eventos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Recital extends Evento {

    private final String genero;
    private final List<Artista> artistas;

    public Recital(int id, String nombre, String descripcion, int duracionMinutos, String genero) {
        super(id, nombre, descripcion, duracionMinutos);
        this.genero = genero == null ? "" : genero.trim();
        this.artistas = new ArrayList<>();
    }

    public void agregarArtista(Artista artista) {
        if (artista == null) {
            throw new IllegalArgumentException("El artista no puede ser nulo");
        }
        if (!artistas.contains(artista)) {
            artistas.add(artista);
        }
    }

    public List<Artista> getArtistas() {
        return Collections.unmodifiableList(artistas);
    }

    public String getGenero() {
        return genero;
    }

    @Override
    public String getTipo() {
        return "Recital";
    }
}
