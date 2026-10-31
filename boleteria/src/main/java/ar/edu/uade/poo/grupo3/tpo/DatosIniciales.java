package ar.edu.uade.poo.grupo3.tpo;

import ar.edu.uade.poo.grupo3.tpo.controlador.Boleteria;
import ar.edu.uade.poo.grupo3.tpo.enums.TipoSector;
import ar.edu.uade.poo.grupo3.tpo.eventos.Artista;
import ar.edu.uade.poo.grupo3.tpo.eventos.Funcion;
import ar.edu.uade.poo.grupo3.tpo.eventos.Preventa;
import ar.edu.uade.poo.grupo3.tpo.eventos.Recital;
import ar.edu.uade.poo.grupo3.tpo.infraestructura.SectorFactory;
import ar.edu.uade.poo.grupo3.tpo.infraestructura.Sede;
import ar.edu.uade.poo.grupo3.tpo.promociones.Promocion;
import ar.edu.uade.poo.grupo3.tpo.reglas.NoDejarAsientoSuelto;
import ar.edu.uade.poo.grupo3.tpo.reglas.RequiereCodigoPreventa;
import ar.edu.uade.poo.grupo3.tpo.ventas.Cargo;
import ar.edu.uade.poo.grupo3.tpo.ventas.PaqueteVIP;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

/**
 * Configura la boletería: carga los .txt de la carpeta datos/ y, si están vacíos, crea datos de ejemplo.
 * Los eventos y funciones todavía se crean en memoria (su persistencia queda para la entrega final).
 */
public final class DatosIniciales {

    public static final String CONTRASENIA_DEMO = "boleteria123";

    private DatosIniciales() {
    }

    public static void inicializar(Boleteria boleteria) throws IOException {
        boleteria.agregarRegla(new RequiereCodigoPreventa());
        boleteria.agregarRegla(new NoDejarAsientoSuelto());
        boleteria.agregarCargo(new Cargo("Cargo por servicio", 12));
        boleteria.agregarPromocion(new Promocion("BANCO20", 20, "Promo bancaria"));
        boleteria.agregarPromocion(new Promocion("FAN10", 10, "Club de fans"));

        PaqueteVIP gold = new PaqueteVIP("Experiencia Gold", 85000, 20);
        gold.agregarBeneficio("Ingreso anticipado");
        gold.agregarBeneficio("Lounge con bebidas");
        gold.agregarBeneficio("Merchandising exclusivo");
        boleteria.agregarPaquete(gold);

        boleteria.cargarDatos();
        if (boleteria.getSedes().isEmpty()) {
            crearSedesDeEjemplo(boleteria);
            boleteria.guardarSedes();
        }
        if (boleteria.getClientes().isEmpty()) {
            boleteria.registrarCuenta("30111222", "Ana García", "ana@mail.com", CONTRASENIA_DEMO);
            boleteria.registrarCuenta("28999111", "Bruno Díaz", "bruno@mail.com", CONTRASENIA_DEMO);
            boleteria.registrarCuenta("35444777", "Carla Méndez", "carla@mail.com", CONTRASENIA_DEMO);
            boleteria.guardarClientes();
        }
        crearEventosDeEjemplo(boleteria);
    }

    private static void crearSedesDeEjemplo(Boleteria boleteria) {
        Sede esfera = new Sede("Esfera Las Vegas", "255 Sands Ave, Las Vegas, NV");
        esfera.agregarSector(SectorFactory.crearNumerado("FLR", "Floor", 0, 5, 8, 16));
        esfera.agregarSector(SectorFactory.crearNumerado("100", "Nivel 100", 1, 15, 6, 20));
        esfera.agregarSector(SectorFactory.crearNumerado("200", "Nivel 200", 2, 30, 6, 24));
        esfera.agregarSector(SectorFactory.crearNumerado("300", "Nivel 300", 3, 45, 5, 28));
        esfera.agregarSector(SectorFactory.crearNumerado("400", "Nivel 400", 4, 60, 5, 30));
        boleteria.agregarSede(esfera);

        Sede estadio = new Sede("Estadio Monumental", "Av. Figueroa Alcorta 7597, CABA");
        estadio.agregarSector(SectorFactory.crear(TipoSector.GENERAL, "CAMPO", "Campo", 0, 10, 0, 0, 5000));
        estadio.agregarSector(SectorFactory.crearNumerado("PSM", "Platea San Martín", 1, 40, 10, 30));
        estadio.agregarSector(SectorFactory.crearNumerado("PBE", "Platea Belgrano", 1, 40, 10, 30));
        boleteria.agregarSede(estadio);
    }

    private static void crearEventosDeEjemplo(Boleteria boleteria) {
        Optional<Sede> esfera = boleteria.buscarSede("Esfera Las Vegas");
        if (esfera.isEmpty()) {
            return;
        }
        Recital recital = new Recital(1, "Los Satélites — Órbita Tour",
                "Show inmersivo en 360°", 120, "Rock");
        recital.agregarArtista(new Artista("Los Satélites", "Argentina"));
        LocalDate hoy = LocalDate.now();
        Funcion f1 = new Funcion(1, recital, LocalDateTime.of(hoy.plusDays(15), LocalTime.of(21, 0)),
                50000, esfera.get());
        Funcion f2 = new Funcion(2, recital, LocalDateTime.of(hoy.plusDays(30), LocalTime.of(21, 0)),
                50000, esfera.get());
        f2.setPreventa(new Preventa("ORBITA26", LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(3)));
        Funcion f3 = new Funcion(3, recital, LocalDateTime.of(hoy.plusDays(1), LocalTime.of(21, 0)),
                50000, esfera.get());
        recital.agregarFuncion(f1);
        recital.agregarFuncion(f2);
        recital.agregarFuncion(f3);
        boleteria.agregarEvento(recital);
    }
}
