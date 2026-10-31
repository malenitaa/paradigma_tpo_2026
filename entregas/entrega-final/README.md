# Entrega final — Documentación

**Fecha:** 18/11/2026 · **Estado:** borrador en revisión del grupo

| Archivo | Qué es |
|---|---|
| [`TPO POO - Grupo 3 - Documentacion.pdf`](TPO%20POO%20-%20Grupo%203%20-%20Documentacion.pdf) | Documento de ≈10 páginas: propuesta, flujo de compra, diagrama, clases, patrones, excepciones, persistencia, estructura y capturas |
| [`TPO POO - Grupo 3 - Documentacion.docx`](TPO%20POO%20-%20Grupo%203%20-%20Documentacion.docx) | El mismo documento, editable |

Para la defensa oral, el **por qué** de cada decisión está en [`DEFENSA.md`](../../DEFENSA.md).

## Pendiente de actualizar en el documento

- La sección 8 (estructura de archivos) describe la organización anterior: `Boleteria` en un paquete
  `controlador` y las pantallas en `ui`. Desde el PR *Reorganizar el código en las tres capas de MVC*
  el código está en `vista/`, `controlador/` (`SedeController`, `VentaController`) y `modelo/`
  (donde ahora vive `Boleteria`). Ver [`boleteria/README.md`](../../boleteria/README.md).
- La fila "MVC" de la sección 5 menciona solo `SedeController`; falta `VentaController`.
- Capturas: faltan la pantalla de Devoluciones y los *dialogs* cuando se hagan.
