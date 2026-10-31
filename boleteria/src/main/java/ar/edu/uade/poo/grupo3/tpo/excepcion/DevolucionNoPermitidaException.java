package ar.edu.uade.poo.grupo3.tpo.excepcion;

/** La entrada no se puede devolver (no está pagada o faltan menos de 48 hs para la función). */
public class DevolucionNoPermitidaException extends Exception {

    public DevolucionNoPermitidaException(String mensaje) {
        super(mensaje);
    }
}
