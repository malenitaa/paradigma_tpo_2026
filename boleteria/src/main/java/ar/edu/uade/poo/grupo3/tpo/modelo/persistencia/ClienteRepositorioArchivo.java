package ar.edu.uade.poo.grupo3.tpo.modelo.persistencia;

import ar.edu.uade.poo.grupo3.tpo.modelo.ventas.Cliente;
import ar.edu.uade.poo.grupo3.tpo.modelo.ventas.Cuenta;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Guarda los clientes y sus cuentas en datos/clientes.txt: dni|nombre|email|hashContraseña. */
public class ClienteRepositorioArchivo implements Repositorio<Cliente> {

    private final Path archivo;

    public ClienteRepositorioArchivo(Path directorioDatos) {
        this.archivo = directorioDatos.resolve("clientes.txt");
    }

    @Override
    public List<Cliente> cargarTodos() throws IOException {
        List<Cliente> clientes = new ArrayList<>();
        for (String linea : SedeRepositorioArchivo.leerLineas(archivo)) {
            String[] p = linea.split("\\|", -1);
            if (p.length >= 4) {
                clientes.add(new Cliente(p[0], p[1], Cuenta.desdeHash(p[2], p[3])));
            }
        }
        return clientes;
    }

    @Override
    public void guardarTodos(List<Cliente> clientes) throws IOException {
        Files.createDirectories(archivo.getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(archivo, StandardCharsets.UTF_8)) {
            writer.write("# dni|nombre|email|hashContraseña(SHA-256)");
            writer.newLine();
            for (Cliente c : clientes) {
                writer.write(String.join("|", c.getDni(), c.getNombre(), c.getEmail(),
                        c.getCuenta().getHashContrasenia()));
                writer.newLine();
            }
        }
    }
}
