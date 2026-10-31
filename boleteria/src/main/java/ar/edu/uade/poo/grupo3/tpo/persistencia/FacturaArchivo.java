package ar.edu.uade.poo.grupo3.tpo.persistencia;

import ar.edu.uade.poo.grupo3.tpo.ventas.Factura;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Guarda cada comprobante como datos/facturas/FAC-0001-00000001.txt. */
public class FacturaArchivo {

    private final Path carpeta;

    public FacturaArchivo(Path directorioDatos) {
        this.carpeta = directorioDatos.resolve("facturas");
    }

    public Path guardar(Factura factura) throws IOException {
        Files.createDirectories(carpeta);
        Path archivo = carpeta.resolve("FAC-" + factura.getNumero() + ".txt");
        Files.writeString(archivo, factura.generarTexto(), StandardCharsets.UTF_8);
        return archivo;
    }
}
