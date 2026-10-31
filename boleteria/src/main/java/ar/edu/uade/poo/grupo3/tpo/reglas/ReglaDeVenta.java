package ar.edu.uade.poo.grupo3.tpo.reglas;

import ar.edu.uade.poo.grupo3.tpo.eventos.Funcion;
import ar.edu.uade.poo.grupo3.tpo.excepcion.SeleccionInvalidaException;
import ar.edu.uade.poo.grupo3.tpo.infraestructura.Asiento;
import ar.edu.uade.poo.grupo3.tpo.infraestructura.Sector;
import ar.edu.uade.poo.grupo3.tpo.ventas.Compra;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Patrón Strategy: cada regla se valida antes de reservar. La Boleteria recorre la lista de reglas
 * sin saber cuáles son, así se pueden agregar reglas nuevas sin modificarla.
 */
public interface ReglaDeVenta {

    void validar(Compra compra, Funcion funcion, Sector sector, List<Asiento> seleccion, LocalDateTime ahora)
            throws SeleccionInvalidaException;
}
