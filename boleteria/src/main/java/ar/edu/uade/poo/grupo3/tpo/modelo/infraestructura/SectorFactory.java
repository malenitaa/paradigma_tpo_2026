package ar.edu.uade.poo.grupo3.tpo.modelo.infraestructura;

import ar.edu.uade.poo.grupo3.tpo.modelo.enums.TipoSector;

/**
 * Patrón Factory: centraliza la creación de sectores. Lo usan tanto las pantallas Swing
 * como el repositorio que lee los archivos, así nadie hace "new SectorNumerado" por su cuenta.
 */
public final class SectorFactory {

    private SectorFactory() {
    }

    /**
     * @param filas            solo para sectores numerados
     * @param asientosPorFila  solo para sectores numerados
     * @param capacidad        solo para sectores generales
     */
    public static Sector crear(TipoSector tipo, String codigo, String nombre, int piso, double distancia,
                               int filas, int asientosPorFila, int capacidad) {
        if (tipo == null) {
            throw new IllegalArgumentException("Elegí el tipo de sector");
        }
        return switch (tipo) {
            case NUMERADO -> crearNumerado(codigo, nombre, piso, distancia, filas, asientosPorFila);
            case GENERAL -> new SectorGeneral(codigo, nombre, piso, distancia, capacidad);
        };
    }

    public static SectorNumerado crearNumerado(String codigo, String nombre, int piso, double distancia,
                                               int filas, int asientosPorFila) {
        SectorNumerado sector = new SectorNumerado(codigo, nombre, piso, distancia);
        sector.generarAsientos(filas, asientosPorFila);
        return sector;
    }
}
