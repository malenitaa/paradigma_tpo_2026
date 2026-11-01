# Entrega parcial 2 — Avance de código + interfaces gráficas

**Fecha:** 01/11/2026 · **Estado:** en revisión del grupo

El código está en [`boleteria/`](../../boleteria/). Esta tabla muestra dónde está cada punto de la consigna.

| Pedido de la consigna | Dónde está |
|---|---|
| Código de las clases del diagrama, con los ajustes de la entrega 1 | `boleteria/src/main/java/ar/edu/uade/poo/grupo3/tpo/` · diagrama actualizado: [`diagrama_clases_v4`](../../diagramas/clases/diagrama_clases_v4.svg) |
| Al menos 3 patrones / buenas prácticas implementados | Singleton (`Boleteria`), Strategy (`ReglaDeVenta`), Factory (`Sector.crear`), Repository (`Repositorio<T>`), MVC con paquetes `vista` / `controlador` / `modelo` ([diagrama](../../diagramas/arquitectura/mvc.svg)), GRASP Controlador / Creador / Experto |
| Excepción personalizada del negocio | `SeleccionInvalidaException`, `ReservaVencidaException`, `DevolucionNoPermitidaException` |
| Al menos 3 try/catch | Pantallas de alta, modificación, baja y venta; inicio de la app; guardado de facturas; `DemoFlujoCompra` |
| 4 pantallas Swing de ABM | Sedes: Consulta, Alta, Modificación y Baja ([capturas](../../capturas/)) |
| Primer avance de persistencia | `boleteria/datos/`: `sedes.txt`, `sectores.txt`, `clientes.txt` y `facturas/` |

**Adelanto de la entrega final:** la pantalla de **Venta de entradas** (mapa de asientos, reserva de 5 minutos,
promoción, pago y factura) ya funciona.

## Qué falta para la entrega final

- [x] Pantalla de **Devoluciones** (`PanelDevolucion`: verificar la entrada por código y devolverla)
- [ ] Guardar eventos y funciones en archivos (hoy se crean en memoria en `DatosIniciales`)
- [x] Al menos 3 *dialogs*: confirmación de baja (`showConfirmDialog`), error de arranque (`showMessageDialog`) y el comprobante (`FacturaDialog`, un `JDialog` modal)
- [x] Documentación de ≈10 páginas: propuesta, diagrama, estructura del código y capturas

## Formato de entrega

Código fuente Java (`boleteria/`) y archivos de datos (`boleteria/datos/`).
