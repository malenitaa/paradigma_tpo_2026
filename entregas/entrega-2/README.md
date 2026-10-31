# Entrega parcial 2 — Avance de código + interfaces gráficas

**Fecha:** 01/11/2026 · **Estado:** en revisión del grupo

El código está en [`boleteria/`](../../boleteria/). Esta tabla muestra dónde está cada punto de la consigna.

| Pedido de la consigna | Dónde está |
|---|---|
| Código de las clases del diagrama, con los ajustes de la entrega 1 | `boleteria/src/main/java/ar/edu/uade/poo/grupo3/tpo/` · diagrama actualizado: [`diagrama_clases_v4`](../../diagramas/clases/diagrama_clases_v4.png) |
| Al menos 3 patrones / buenas prácticas implementados | Singleton (`Boleteria`), Strategy (`ReglaDeVenta`), Factory (`SectorFactory`), Repository (`Repositorio<T>`), MVC en el ABM, GRASP Controlador / Creador / Experto |
| Excepción personalizada del negocio | `SeleccionInvalidaException`, `ReservaVencidaException`, `DevolucionNoPermitidaException`, `AutenticacionException` |
| Al menos 3 try/catch | Pantallas de alta, modificación, baja y venta; inicio de la app; guardado de facturas; `DemoFlujoCompra` |
| 4 pantallas Swing de ABM | Sedes: Consulta, Alta, Modificación y Baja ([capturas](../../capturas/)) |
| Primer avance de persistencia | `boleteria/datos/`: `sedes.txt`, `sectores.txt`, `clientes.txt` y `facturas/` |

**Adelanto de la entrega final:** la pantalla de **Venta de entradas** (mapa de asientos, reserva de 5 minutos,
promoción, pago y factura) ya funciona.

## Qué falta para la entrega final

- [ ] Pantalla de **Devoluciones** (la lógica ya está en `Boleteria.devolverEntrada`)
- [ ] Guardar eventos y funciones en archivos (hoy se crean en memoria en `DatosIniciales`)
- [ ] Al menos 3 *dialogs*
- [ ] Documentación de ≈10 páginas: propuesta, diagrama, estructura del código y capturas

## Formato de entrega

Código fuente Java (`boleteria/`) y archivos de datos (`boleteria/datos/`).
