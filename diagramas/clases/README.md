# Diagramas de clases · Entrega final

## Archivos

| Archivo | Uso |
|---------|-----|
| `diagrama_clases_final.puml` | **Entrega actual** — alineado al código simplificado de `boleteria/` y a la interfaz Swing + MVC. |
| `diagrama_clases_entrega2.puml` | Versión de la Entrega 2 (con preventa, paquete VIP, reglas de venta e inicio de sesión). |
| `diagrama_clases_v4.puml` | Versión anterior centrada solo en el dominio (sin capa vista/controlador explícita). |
| `diagrama_clases_v3.puml` | Histórico previo a v4. |

Imagen exportada: `diagrama_clases_final.png` (desde el `.puml`).

Regenerar (requiere Java; sin Graphviz usar layout Smetana):

```bash
java -jar plantuml.jar -tpng -Playout=smetana diagramas/clases/diagrama_clases_final.puml
mv diagramas/clases/TPO_diagrama_clases_final.png diagramas/clases/diagrama_clases_final.png
```

## Cambios respecto al diagrama original grupo 3 (imagen TPO)

- **Eliminado:** subsistema de **estacionamiento** (`Estacionamiento`, reservas de cochera, `Vehículo`, etc.).
- **Eliminado / fusionado en el código:** `Recital`, `Artista`, `Cuenta`, clase `Pago`, `Cargo`, `SectorFactory`, `FacturaArchivo`, `AutenticacionException`, precios dinámicos y cargos adicionales del diseño inicial.
- **Agregado:** reserva de compra (**5 minutos**), estados (`EstadoCompra`, `EstadoEntrada`, `EstadoFuncion`), **Devolución** (48 h), **Factura** con persistencia en archivo, **Promoción** por código, **Paquete VIP**, **Preventa** con código, reglas `NoDejarAsientoSuelto` y `RequiereCodigoPreventa`, persistencia `Repositorio` + repos de sedes/clientes.
- **Agregado (Entrega 2):** capa **MVC** — `SedeController`, `VentaController`, vista **Swing** (`VentanaPrincipal`, paneles, `FacturaDialog`).
- **Modelo:** `Boleteria` como **Singleton / fachada** del dominio (no es el controlador MVC); los controladores delegan en ella.
- **Sectores:** jerarquía `Sector` → `SectorNumerado` / `SectorGeneral`, creación con `Sector.crear` y enum `TipoSector`.
- **Quitado (Entrega final):** `PaqueteVIP`, `Preventa`, `ReglaDeVenta`, `NoDejarAsientoSuelto`, `RequiereCodigoPreventa`, `PanelLoginCliente`, `PanelRegistroCliente`, `EventoRepositorioArchivo` y la demo por consola. En la venta el cliente se elige de una lista y los eventos se crean en memoria al arrancar.

## Clases incluidas en `diagrama_clases_final.puml`

**Vista:** `BoleteriaApp`, `VentanaPrincipal`, `Pantalla`, paneles ABM/consulta/venta/devolución, `FormularioSector`, `FacturaDialog`.

**Controlador:** `SedeController`, `VentaController`.

**Modelo:** `Boleteria`; infraestructura (`Sede`, `Sector`, `SectorNumerado`, `SectorGeneral`, `Asiento`, `TipoSector`); eventos (`Evento`, `Funcion`, `EstadoFuncion`); ventas (`Cliente`, `Compra`, `Entrada`, `Factura`, `Devolucion`, enums de estado y pago); `Promocion`; excepciones; persistencia.

## Clases del código no dibujadas (a propósito)

- `DatosIniciales` — datos de ejemplo al arrancar.
- `Estilo`, `Aviso` — utilidades visuales menores.

## Discrepancias código ↔ diagrama

1. **`Boleteria` en v4:** figuraba como `<<controller>>`; en el código y en el diagrama final es **modelo (Singleton/fachada)**; el MVC lo hacen `SedeController` y `VentaController`.
2. **`Promocion`:** en v4 estaba dentro del paquete lógico “Ventas”; en Java está en `modelo.promociones` — el diagrama final usa paquete **Promociones**.
3. **Excepciones:** en v4 compartían paquete con reglas; en Java están en `modelo.excepcion` — el diagrama final las separa.
4. **Métodos omitidos en el diagrama** (legibilidad): getters, `calcularSubtotalEntradas`, `getSegundosRestantes`, `cancelarCompra`, búsquedas auxiliares de `Boleteria`, etc. — el comportamiento está en el código fuente.
5. **`PanelInicio` → `Boleteria`:** el panel de inicio lee las métricas directamente de la fachada del modelo.
6. **Persistencia:** se guardan **sedes/sectores** y **clientes**; eventos, funciones, compras, entradas y promociones viven en memoria en runtime (los eventos se crean al arrancar). Las facturas se escriben como `.txt` en `datos/facturas/`.