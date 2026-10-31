package ar.edu.uade.poo.grupo3.tpo.modelo.excepcion;

/** Email o contraseña incorrectos al iniciar sesión. */
public class AutenticacionException extends Exception {

    public AutenticacionException(String mensaje) {
        super(mensaje);
    }
}
