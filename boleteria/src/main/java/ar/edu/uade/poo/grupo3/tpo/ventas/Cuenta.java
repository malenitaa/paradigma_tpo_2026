package ar.edu.uade.poo.grupo3.tpo.ventas;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.regex.Pattern;

/** Cuenta de usuario del cliente: email + contraseña (se guarda solo el hash, nunca la contraseña). */
public class Cuenta {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s|]+@[^@\\s|]+\\.[^@\\s|]+$");
    public static final int LARGO_MINIMO_CONTRASENIA = 6;

    private final String email;
    private String hashContrasenia;

    /** Crea una cuenta nueva a partir de la contraseña en texto plano. */
    public Cuenta(String email, String contrasenia) {
        this.email = validarEmail(email);
        cambiarContrasenia(contrasenia);
    }

    /** Reconstruye una cuenta leída del archivo (ya viene con el hash). */
    public static Cuenta desdeHash(String email, String hashContrasenia) {
        Cuenta cuenta = new Cuenta(email, "temporal");
        cuenta.hashContrasenia = hashContrasenia;
        return cuenta;
    }

    public boolean verificarContrasenia(String contrasenia) {
        return contrasenia != null && hashContrasenia.equals(hashear(contrasenia));
    }

    public void cambiarContrasenia(String contrasenia) {
        if (contrasenia == null || contrasenia.length() < LARGO_MINIMO_CONTRASENIA) {
            throw new IllegalArgumentException(
                    "La contraseña debe tener al menos " + LARGO_MINIMO_CONTRASENIA + " caracteres");
        }
        this.hashContrasenia = hashear(contrasenia);
    }

    private static String validarEmail(String email) {
        if (email == null || !EMAIL.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("El email no tiene un formato válido");
        }
        return email.trim().toLowerCase();
    }

    private static String hashear(String texto) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(texto.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    public String getEmail() {
        return email;
    }

    public String getHashContrasenia() {
        return hashContrasenia;
    }
}
