# Boletería — Documento de diseño y defensa

**TPO Paradigma Orientado a Objetos · UADE 2026 · Grupo 3**
Aded Fermín · Ávila Tomás · Vázquez Daniel · Villa Abrille Malena

Este documento explica **por qué** el sistema está hecho como está: qué problema resuelve, cómo se
reparte el código, por qué existe cada clase, qué patrones y principios aplicamos y qué responder
cuando pregunten "¿y por qué no lo hicieron de otra forma?". Usa los nombres de la cátedra
(GRASP, SOLID, relaciones UML) y señala en qué archivo está cada cosa.

> Para ejecutar la app y ver las pantallas: [`boleteria/README.md`](boleteria/README.md).
> Documento formal de la entrega final (PDF y DOCX): [`entregas/entrega-final/`](entregas/entrega-final/).
> Diagramas: [clases v4](diagramas/clases/diagrama_clases_v4.png) · [capas MVC](diagramas/arquitectura/mvc.puml) ·
> [secuencia de compra](diagramas/flujo-de-compra/secuencia_compra.png) · [estados](diagramas/flujo-de-compra/estados_compra_entrada.png).

---

## Índice

1. [El problema y la solución](#1-el-problema-y-la-solución)
2. [Cómo se lee el código: las tres capas](#2-cómo-se-lee-el-código-las-tres-capas)
3. [El dominio, clase por clase](#3-el-dominio-clase-por-clase)
4. [Los patrones, uno por uno](#4-los-patrones-uno-por-uno)
5. [GRASP en el proyecto](#5-grasp-en-el-proyecto)
6. [SOLID en el proyecto](#6-solid-en-el-proyecto)
7. [Excepciones: cuáles, por qué y dónde se atrapan](#7-excepciones-cuáles-por-qué-y-dónde-se-atrapan)
8. [El flujo de compra y la reserva de 5 minutos](#8-el-flujo-de-compra-y-la-reserva-de-5-minutos)
9. [Persistencia en archivos](#9-persistencia-en-archivos)
10. [Relaciones del diagrama de clases](#10-relaciones-del-diagrama-de-clases)
11. [Decisiones de implementación en Java](#11-decisiones-de-implementación-en-java)
12. [Qué sacamos de la entrega 1 y por qué](#12-qué-sacamos-de-la-entrega-1-y-por-qué)
13. [Limitaciones conocidas y lo que falta](#13-limitaciones-conocidas-y-lo-que-falta)
14. [Preguntas probables en la defensa](#14-preguntas-probables-en-la-defensa)

---

## 1. El problema y la solución

**El problema.** Una sede grande (la referencia es la Esfera de Las Vegas) vende miles de entradas por
función. El precio de cada ubicación depende del piso y de la distancia al escenario, y a la entrada se
le suman paquetes VIP y cargos por servicio. Gestionado a mano aparecen errores concretos: asientos
vendidos dos veces, precios mal calculados, asientos sueltos que nadie compra y preventas sin control
de acceso.

**La solución.** Una aplicación de escritorio en Java (Swing) para el personal de boletería que
centraliza sedes, sectores, eventos y funciones, y vende entradas calculando el precio y validando las
reglas de venta. Flujo completo: elegir función y sector, marcar asientos, reservarlos **5 minutos**,
aplicar un código promocional, pagar, recibir la factura, y devolver hasta 48 hs antes.

**Fuera de alcance** (decidido en la entrega 1): el contenido del show, el cobro real con pasarela de pago
y la reventa. El `Pago` registra el medio y el número de operación, no cobra.

**Por qué acotamos.** La devolución de la entrega 1 pidió dos cosas: centrar el proyecto en la venta de
entradas y armar el flujo de compra como un caso real. Sacamos lo que no sumaba a ese flujo
(estacionamiento, experiencia inmersiva, precio dinámico) y agregamos lo que sí: reserva por tiempo,
pago, factura, devolución, promociones y cuentas de usuario. El detalle está en la [sección 12](#12-qué-sacamos-de-la-entrega-1-y-por-qué).

---

## 2. Cómo se lee el código: las tres capas

El paquete raíz es `ar.edu.uade.poo.grupo3.tpo` y está dividido en **vista**, **controlador** y
**modelo**, que son las tres capas de MVC:

```
vista/         Pantallas Swing. Arman la pantalla, muestran mensajes. No conocen reglas de negocio.
controlador/   SedeController y VentaController. Reciben lo que hizo el usuario y se lo piden al modelo.
modelo/        Boleteria (fachada) + todo el dominio + persistencia. No sabe que existe Swing.
```

### Por qué MVC

La cátedra lo plantea con el patrón GRASP **Controlador**: "un objeto que NO es de la interfaz recibe el
evento y lo delega al modelo; la UI no conoce reglas de negocio; se puede cambiar consola por Swing sin
tocar el modelo". Acá eso se cumple literalmente: `DemoFlujoCompra` (consola) y `BoleteriaApp` (Swing)
usan **la misma** `Boleteria`. Si mañana la venta se hace por web, el modelo no cambia.

### Quién hace qué

| Capa | Clases | Responsabilidad | Lo que **no** hace |
|---|---|---|---|
| **Vista** | `VentanaPrincipal`, `PanelInicio`, `PanelConsultaSedes`, `PanelAltaSede`, `PanelModificacionSede`, `PanelBajaSede`, `FormularioSector`, `PanelVenta`, `FacturaDialog`, `Aviso`, `Estilo`, `BoleteriaApp` | Dibujar componentes, leer lo que el usuario cargó, mostrar mensajes de éxito/error | Validar reglas, decidir si una compra vence, guardar archivos |
| **Controlador** | `SedeController`, `VentaController` | Un método por acción del usuario (`alta`, `baja`, `reservar`, `pagar`…). Coordinan: llaman al modelo y, en el ABM, piden guardar. `VentaController` además guarda el estado de **la compra en curso** y el reloj de la reserva | Lógica de negocio (eso vive en el modelo) ni dibujar nada |
| **Modelo** | `Boleteria` y los paquetes `infraestructura`, `eventos`, `ventas`, `promociones`, `reglas`, `excepcion`, `enums`, `persistencia` | Todo el dominio: precios, disponibilidad, reglas, estados, persistencia | Conocer Swing o a los controladores |

### Boleteria: ¿controlador o modelo?

Las dos cosas, pero en sentidos distintos, y conviene decirlo claro en la defensa:

- Es el **Controlador de GRASP**: "primer objeto después de la UI que recibe un evento del sistema;
  coordina el caso de uso y delega, no hace la lógica". `Boleteria.reservarAsientos` no decide si un
  asiento queda suelto: le pregunta a las reglas; no sabe si un asiento está libre: le pregunta a la
  `Funcion`.
- En términos de **MVC** es parte del **modelo**: es la fachada del dominio, la única puerta de entrada.
  Los controladores de MVC (`SedeController`, `VentaController`) son los que están entre la pantalla y
  la boletería.

Por eso está en el paquete `modelo` y no en `controlador`. En la entrega 2 original estaba en un paquete
llamado `controlador`, lo movimos justamente para que no se confunda con el controlador de MVC.

### Cómo se arma la aplicación

`BoleteriaApp.main` → `Boleteria.getInstancia()` → `DatosIniciales.inicializar(boleteria)` (carga
archivos, registra reglas, cargos, promos) → `new VentanaPrincipal(boleteria)`. La ventana crea **un
controlador por caso de uso** (`SedeController`, `VentaController`) y se lo pasa a las pantallas. Cada
pantalla implementa la interfaz `Pantalla` con un único método `alMostrar()`, que refresca los datos
cada vez que se entra a esa pantalla (así, si diste de alta una sede, la consulta la muestra sin
reiniciar).

---

## 3. El dominio, clase por clase

Cada clase existe porque hay algo del negocio que **sabe** o **hace** y nadie más debería hacerlo
(GRASP Experto). Si en la defensa preguntan "¿para qué está X?", la respuesta es la columna "Por qué existe".

### `modelo/infraestructura` — dónde se hace el show

| Clase | Qué sabe / hace | Por qué existe así |
|---|---|---|
| `Sede` | Nombre, dirección y sus sectores. Capacidad total. | Es el "todo" de la composición con `Sector`: los sectores no existen sin la sede. Valida que el nombre no tenga `\|` porque es el separador del archivo. |
| `Sector` (abstracta) | Código, nombre, piso, distancia al escenario. **Calcula su multiplicador de precio.** | Abstracta porque hay dos tipos reales con capacidad distinta. El multiplicador vive acá porque el sector es el que **tiene** piso y distancia (Experto). |
| `SectorNumerado` | Lista de `Asiento`. Genera filas A, B, C… | En la Esfera todos los sectores son así. `getCapacidad()` = cantidad de asientos. |
| `SectorGeneral` | Solo capacidad. | Un campo de pie: no hay asiento. **Sobrescribe** `calcularMultiplicador()` para cobrar 20 % menos: estar de pie vale menos que una butaca en el mismo lugar (polimorfismo con comportamiento propio, no un `if` por tipo). |
| `Asiento` | Fila y número. Si es adyacente a otro. | **No sabe si está vendido.** El mismo asiento físico se vende por separado en cada función, así que la disponibilidad la sabe `Funcion`. Tiene `equals`/`hashCode` por fila+número para poder usarlo en `Set` y `contains`. |
| `SectorFactory` | Crea sectores de cualquier tipo a partir de datos sueltos. | Las pantallas y el repositorio de archivos necesitaban crear sectores; sin la fábrica habría dos lugares con el mismo `switch` por tipo. |

**Fórmula de precio.** `Funcion.calcularPrecio(sector) = round(precioBase × sector.calcularMultiplicador())`.
El multiplicador combina cercanía y piso: `porCercania = 1 + 1 / (1 + distancia/10)` (de 2,0 pegado al
escenario a ~1,0 lejos) y `porPiso = max(0,6; 1 − piso × 0,1)`. Un sector general multiplica además por
0,8. Está en `Sector.java` y `SectorGeneral.java`.

### `modelo/eventos` — qué se vende

| Clase | Qué sabe / hace | Por qué existe así |
|---|---|---|
| `Evento` (abstracta) | Nombre, descripción, duración, sus funciones. | Abstracta porque un evento siempre es de algún tipo concreto; `getTipo()` es abstracto. Hoy solo hay `Recital`, pero la jerarquía está lista para agregar otro tipo sin tocar `Boleteria`. |
| `Recital` | Género y artistas. | El tipo de evento del caso de uso principal. |
| `Artista` | Nombre y nacionalidad. `equals` por nombre sin distinguir mayúsculas. | Para que un recital no tenga dos veces al mismo artista. |
| `Funcion` | Fecha, precio base, sede, estado, preventa opcional y **sus entradas**. Sabe si un asiento está disponible, cuántos lugares quedan y el precio por sector. | Es **la experta en disponibilidad**: tiene las entradas emitidas, así que es la única que puede responder "¿está libre A-3?". Pasa a `AGOTADA` sola cuando no queda lugar y vuelve a `PROGRAMADA` si se libera uno. |
| `Preventa` | Código de acceso y período. | Separada de `Funcion` porque es opcional (0..1) y tiene su propia validación de fechas. No cambia el precio: solo restringe quién puede comprar. |

### `modelo/ventas` — la compra

| Clase | Qué sabe / hace | Por qué existe así |
|---|---|---|
| `Cliente` | DNI, nombre, su `Cuenta` y su historial de compras. | Los datos de la persona. DNI validado con 7 u 8 dígitos. |
| `Cuenta` | Email y **hash** de la contraseña. Verifica y cambia contraseña. | Separada de `Cliente` por responsabilidad única: la identidad (DNI, nombre) y el acceso (email, contraseña) cambian por motivos distintos. Guarda SHA-256, nunca la contraseña, porque el archivo `clientes.txt` es texto plano. |
| `Compra` | Entradas, paquetes, cargos, promoción, pago, estado y **vencimiento de la reserva**. Calcula subtotal, descuento, cargos y total. | Es la experta en el total porque tiene todas las partes. **Crea sus entradas** (Creador). Controla su ciclo de vida con `verificarReservada()`: no se puede agregar nada a una compra que ya no está reservada. |
| `Entrada` | Función, sector, asiento (null si es general), **precio congelado** y estado. Sabe si puede devolverse. | El precio se calcula y se guarda al crearla: si después cambia el precio base de la función, lo que pagó el cliente no cambia. |
| `Cargo` | Nombre y porcentaje. `aplicar(monto)`. | Cargo por servicio como objeto y no como constante: la boletería tiene una lista de cargos vigentes y se los aplica a cada compra nueva. Agregar un impuesto es agregar un `Cargo`, no tocar código. |
| `PaqueteVIP` | Nombre, precio, beneficios y **cupo**. | El cupo se reserva al agregarlo a una compra y se libera si la compra vence o se cancela. `reservarCupo()` y `liberarCupo()` son de paquete (sin `public`) para que solo `Compra` los use. |
| `Pago` | Número de operación, medio, monto, fecha. | Registro, no cobro. Separado de `Compra` porque una compra reservada todavía no tiene pago (0..1). |
| `Factura` | Número correlativo, fecha, la compra. Genera el texto del comprobante. | Solo se puede crear a partir de una compra pagada (lo valida el constructor). Es la única que sabe cómo se ve un comprobante. |
| `Devolucion` | Entrada, motivo, fecha, monto a reintegrar. | El reintegro es el precio de la entrada **sin** los cargos: la regla está en `calcularReintegro()`, en un solo lugar. |

### `modelo/promociones`, `modelo/reglas`, `modelo/excepcion`, `modelo/enums`

| Clase | Qué sabe / hace | Por qué existe así |
|---|---|---|
| `Promocion` | Código, porcentaje, descripción. Si coincide con un código ingresado y cuánto descuenta. | El descuento se calcula sobre las entradas, no sobre los paquetes VIP: decisión de negocio puesta en `calcularDescuento`. Una compra admite una sola promoción. |
| `ReglaDeVenta` (interfaz) | `validar(compra, funcion, sector, seleccion, ahora)` que lanza `SeleccionInvalidaException`. | Contrato estable: la boletería recorre la lista de reglas sin saber cuáles son. |
| `NoDejarAsientoSuelto` | Rechaza una selección que deje un asiento libre entre dos ocupados (o entre un ocupado y el borde). | Es el problema real de "asientos que nadie compra". Compara el mapa de ocupados antes y después de la selección y solo falla si la selección **crea** un asiento suelto nuevo. |
| `RequiereCodigoPreventa` | Durante la preventa exige el código. | Lee la fecha `ahora` que le pasan, no `LocalDateTime.now()`: así se puede probar con cualquier fecha. |
| `SeleccionInvalidaException`, `ReservaVencidaException`, `DevolucionNoPermitidaException`, `AutenticacionException` | Las cuatro situaciones del negocio que el usuario tiene que ver como mensaje. | Ver [sección 7](#7-excepciones-cuáles-por-qué-y-dónde-se-atrapan). |
| `EstadoFuncion`, `EstadoCompra`, `EstadoEntrada`, `MedioPago`, `TipoSector` | Valores cerrados. | Enums y no strings para que el compilador rechace un estado inexistente. `EstadoEntrada.estaActiva()` muestra que un enum puede tener comportamiento. |

### `modelo/persistencia`

| Clase | Qué hace | Por qué existe así |
|---|---|---|
| `Repositorio<T>` (interfaz) | `cargarTodos()` y `guardarTodos(lista)`. | El dominio no sabe cómo se guardan los datos. Hoy es `.txt`; mañana puede ser una base de datos sin tocar `Boleteria`. |
| `SedeRepositorioArchivo` | Sedes en `sedes.txt`, sectores en `sectores.txt`. | Dos archivos porque son dos entidades con distinta cantidad de campos. Si una línea está mal, avisa con el número de registro. |
| `ClienteRepositorioArchivo` | `clientes.txt` con el hash. | Reconstruye la `Cuenta` con `Cuenta.desdeHash`, sin volver a hashear. |
| `FacturaArchivo` | Un `.txt` por comprobante en `datos/facturas/`. | No implementa `Repositorio` porque las facturas solo se escriben, nunca se cargan: no tiene sentido obligarla a tener `cargarTodos()` (Segregación de interfaces). |

### `Boleteria` (fachada del modelo)

Guarda las colecciones del sistema (sedes, eventos, clientes, compras, facturas, devoluciones, reglas,
cargos, promociones, paquetes) y expone las operaciones: ABM de sedes, cuentas, búsqueda de funciones,
flujo de compra y devoluciones. Todos los getters devuelven listas **no modificables**
(`Collections.unmodifiableList`): nadie de afuera puede meter una sede sin pasar por `agregarSede`, que
es donde están las validaciones.

---

## 4. Los patrones, uno por uno

La consigna pide "al menos 3 patrones / buenas prácticas". Tenemos más, pero conviene saber defender
cada uno con la misma estructura: **qué resuelve, dónde está, qué pasaría sin él**.

### Singleton — `Boleteria.getInstancia()`

- **Qué resuelve.** Hay una sola boletería en todo el sistema y todas las pantallas tienen que ver las
  mismas sedes, compras y reservas. Si cada pantalla tuviera su propia instancia, una reserva hecha en
  la pantalla de venta no se vería desde la consulta.
- **Cómo.** Constructor privado, un atributo estático `instancia` y un método `getInstancia()`
  `synchronized` que la crea la primera vez (creación perezosa).
- **Sin él.** Habría que pasar la misma instancia a mano por todos lados y nada impediría crear otra.
- **Pregunta probable: "¿no es una variable global disfrazada?"** En parte sí, y por eso la usamos solo
  para la fachada. El resto del dominio no sabe que `Boleteria` es singleton: `SedeController` y
  `VentaController` la reciben por constructor, así que en una prueba se les podría pasar otra.

### Controlador (GRASP) — `Boleteria`, `SedeController`, `VentaController`

- **Qué resuelve.** Que la pantalla no contenga reglas de negocio. La cátedra lo define como "primer
  objeto después de la UI que recibe un evento del sistema; coordina y delega".
- **Dónde.** `SedeController.alta(nombre, direccion, sectores)` crea la `Sede`, se la da a la boletería
  (que valida) y pide guardar. `VentaController.reservar(...)` inicia la compra, reserva, agrega el
  paquete y, si una regla falla, cancela la compra y relanza la excepción para que la pantalla muestre
  el mensaje. La pantalla solo hace `controlador.reservar(...)` dentro de un `try`.
- **Por qué dos controladores y no uno.** La cátedra recomienda "dividir los eventos del sistema en el
  mayor número de controladores para aumentar la cohesión". Uno por caso de uso: sedes y venta no
  comparten nada.
- **Qué quedó en `VentaController` y por qué.** La compra en curso (`compra`) y el reloj de la demo
  (`desfaseReloj`). Son estado del caso de uso, no de la pantalla: si la pantalla se cerrara y volviera a
  abrir, la reserva sigue vigente. El `Timer` de Swing sí queda en la vista porque es un componente de
  Swing que solo sirve para repintar el contador cada segundo; lo que significa el contador
  (`segundosRestantes()`, `verificarVencimiento()`) lo responde el controlador.

### MVC — paquetes `vista`, `controlador`, `modelo`

Explicado en la [sección 2](#2-cómo-se-lee-el-código-las-tres-capas). En la defensa alcanza con:
"la vista arma la pantalla y muestra mensajes; el controlador recibe lo que hizo el usuario y se lo pide
al modelo; el modelo tiene las reglas y no sabe que existe Swing. Prueba: la demo por consola usa el mismo
modelo".

### Strategy — `ReglaDeVenta` con `NoDejarAsientoSuelto` y `RequiereCodigoPreventa`

- **Qué resuelve.** Las reglas de venta cambian seguido (hoy son dos, mañana puede haber "máximo 10
  entradas por cliente" o "menores no en el campo"). No queremos un método `reservar` lleno de `if`.
- **Cómo.** Una interfaz con un solo método `validar(...)`. `Boleteria` tiene una `List<ReglaDeVenta>` y
  en `validarReglas` las recorre todas. `DatosIniciales` registra cuáles se usan.
- **Sin él.** Cada regla nueva obliga a modificar `Boleteria.reservarAsientos` (viola Abierto/Cerrado) y
  no se puede probar una regla aislada.
- **En términos de la cátedra.** Es GRASP **Polimorfismo** ("comportamiento según el tipo, sin
  if/switch") y **Variaciones protegidas** ("lo que cambia, detrás de una interfaz estable"). Es el mismo
  esquema que el `Plan` del ejercicio integrador del gimnasio: agregar `PlanPaseLibre` sin tocar nada.
- **Detalle defendible.** La regla recibe `ahora` como parámetro en vez de llamar a `LocalDateTime.now()`.
  Eso permite que la demo pruebe "pasaron 6 minutos" y que un test futuro fije la fecha.

### Factory — `SectorFactory`

- **Qué resuelve.** Crear un sector requiere decidir por tipo (numerado o general), con parámetros
  distintos, y generar los asientos. Eso lo necesitaban dos lugares: `FormularioSector` (pantalla) y
  `SedeRepositorioArchivo` (al leer el archivo).
- **Cómo.** Clase `final` con constructor privado y métodos estáticos `crear(...)` y `crearNumerado(...)`.
  Es el único lugar con `switch (tipo)`.
- **Sin él.** El `switch` duplicado en la pantalla y en el repositorio; agregar un tipo de sector obliga a
  tocar los dos.
- **En términos de la cátedra.** GRASP **Creador** (tiene los datos para inicializar) y **Fabricación
  pura** (no es una clase del negocio: se inventó para ordenar la creación).

### Repository — `Repositorio<T>`, `SedeRepositorioArchivo`, `ClienteRepositorioArchivo`

- **Qué resuelve.** Que `Boleteria` no sepa de archivos, separadores ni `BufferedReader`.
- **Cómo.** Interfaz genérica con `cargarTodos()` / `guardarTodos(lista)`. `Boleteria` tiene atributos del
  tipo de la interfaz (`Repositorio<Sede>`, `Repositorio<Cliente>`), no de las clases concretas.
- **Sin él.** Lógica de archivos mezclada con la de ventas, y para pasar a base de datos habría que
  reescribir `Boleteria`.
- **En términos de la cátedra.** GRASP **Indirección** ("un intermediario desacopla dos componentes") y
  SOLID **Inversión de dependencias** ("el alto nivel depende de la abstracción `Repositorio`"). Es lo
  mismo que `RepositorioSocios` en el integrador del gimnasio.
- **Honestidad.** `Boleteria` todavía hace `new SedeRepositorioArchivo(...)` en su constructor. La
  dependencia es a la interfaz, pero la creación está adentro. Para inversión de dependencias completa
  habría que inyectar los repositorios por constructor; lo dejamos así para mantener el singleton simple.
  Ver [sección 13](#13-limitaciones-conocidas-y-lo-que-falta).

### Herencia y polimorfismo — `Sector` y `Evento`

- `Sector` → `SectorNumerado` / `SectorGeneral`: `getCapacidad()` y `getTipo()` abstractos (cada tipo
  sabe el suyo); `calcularMultiplicador()` tiene implementación en la base y `SectorGeneral` la
  **sobrescribe** llamando a `super` y aplicando el 0,8. Quien calcula un precio no pregunta el tipo.
- `Evento` → `Recital`: `getTipo()` abstracto; `Recital` agrega género y artistas.
- **Por qué clase abstracta y no interfaz.** Porque hay **estado y código común** (código, piso, distancia,
  el multiplicador base). Una interfaz no puede tener atributos. `ReglaDeVenta`, en cambio, es interfaz
  porque no comparte nada más que el contrato.

---

## 5. GRASP en el proyecto

La cátedra enseña ocho patrones GRASP. Dónde está cada uno:

| Patrón | Qué dice (cátedra) | Dónde se ve | Cómo defenderlo |
|---|---|---|---|
| **Experto en información** | La responsabilidad va a quien tiene los datos | `Funcion.estaDisponible` (tiene las entradas) · `Compra.calcularTotal` (tiene entradas, paquetes, cargos, promo) · `Sector.calcularMultiplicador` (tiene piso y distancia) · `Entrada.puedeDevolverse` (tiene estado y función) · `Preventa.estaVigente` | "¿Quién sabe si A-3 está libre? La función, porque tiene sus entradas. Por eso no lo sabe el asiento ni la boletería." |
| **Creador** | B crea A si lo contiene, lo registra, lo usa o tiene los datos | `Compra.agregarEntrada` crea `Entrada` (la contiene) · `Boleteria.iniciarCompra` crea `Compra` y `confirmarCompra` crea `Pago` y `Factura` (las registra) · `SectorNumerado.generarAsientos` crea `Asiento` · `SectorFactory` | "Nadie hace `new Entrada` salvo `Compra`; nadie hace `new Pago` salvo la boletería." |
| **Controlador** | Primer objeto después de la UI; coordina y delega | `SedeController`, `VentaController` y, como fachada, `Boleteria` | Sección 4. |
| **Bajo acoplamiento** | Depender de la menor cantidad de clases, nunca de sus detalles | La vista depende del controlador, no de `Funcion`/`Compra` para operar · `Boleteria` depende de `Repositorio<T>` y `ReglaDeVenta`, no de las implementaciones · `Cliente.getEmail()` oculta que el email está en `Cuenta` | "Agregar una regla o cambiar el formato de archivo no toca ni la vista ni la boletería." |
| **Alta cohesión** | Cada clase hace pocas cosas muy relacionadas | `Cuenta` separada de `Cliente` · `Pago`, `Factura`, `Devolucion` separadas de `Compra` · `Estilo` concentra el look de Swing · `FacturaDialog` solo muestra el comprobante | "Una compra pagada no se vuelve 'factura'; la factura es otro objeto con otra responsabilidad." |
| **Fabricación pura** | Clase inventada (no del dominio) para ordenar el diseño | `SectorFactory`, los repositorios, `FacturaArchivo`, `DatosIniciales`, los controladores, `Estilo` | "No existen en la boletería real; existen para que las clases del negocio queden limpias." |
| **Indirección** | Un intermediario desacopla dos componentes | `Repositorio<T>` entre `Boleteria` y los archivos · los controladores entre Swing y el modelo | Igual que `ServicioLog` en el apunte: si cambia el archivo, cambia solo el repositorio. |
| **Polimorfismo** | Comportamiento según el tipo, sin `if`/`switch` | `ReglaDeVenta.validar`, `Sector.calcularMultiplicador`, `Sector.getCapacidad`, `Evento.getTipo` | "El único `switch` por tipo está en `SectorFactory`, que es justamente el lugar donde se decide qué crear." |
| **Variaciones protegidas** | Lo que cambia, detrás de una interfaz estable | `ReglaDeVenta` (las reglas cambian) · `Repositorio<T>` (el medio de persistencia cambia) · `Pantalla` (cada pantalla refresca distinto) | "Identificamos qué iba a cambiar y lo pusimos detrás de una interfaz." |

---

## 6. SOLID en el proyecto

| Principio | Dónde se cumple | Dónde se podría mejorar (decirlo antes de que lo pregunten) |
|---|---|---|
| **S** Responsabilidad única | `Cuenta` ≠ `Cliente` · `Factura` ≠ `Compra` · `FacturaArchivo` solo escribe · vista ≠ controlador ≠ modelo | `Boleteria` tiene muchas operaciones (es la fachada). Están agrupadas por sección y cada una delega, pero si creciera habría que dividirla en servicios (sedes, cuentas, ventas). |
| **O** Abierto/Cerrado | Regla nueva = clase nueva + una línea en `DatosIniciales` · tipo de evento nuevo = subclase de `Evento` · cargo o promoción nuevos = datos, no código | Un tipo de sector nuevo obliga a tocar el `switch` de `SectorFactory` y el formato del archivo. Es aceptable: es el único lugar. |
| **L** Sustitución de Liskov | `SectorGeneral` y `SectorNumerado` se usan como `Sector` en toda la boletería y en la `Funcion`; ninguna subclase lanza "no soportado" | `Funcion.estaDisponible` pregunta `instanceof SectorNumerado` para saber si hay que validar el asiento. Se justifica porque un sector general no tiene asientos (no es que "no sepa" hacerlo, es que el concepto no existe). |
| **I** Segregación de interfaces | `ReglaDeVenta` y `Pantalla` tienen un solo método · `FacturaArchivo` no implementa `Repositorio` porque no carga | — |
| **D** Inversión de dependencias | `Boleteria` depende de `Repositorio<T>` y `ReglaDeVenta` (interfaces); los controladores reciben `Boleteria` por constructor; las pantallas reciben el controlador por constructor | `Boleteria` crea sus repositorios con `new` en vez de recibirlos. Para un test con repositorio en memoria habría que inyectarlos. |

---

## 7. Excepciones: cuáles, por qué y dónde se atrapan

### Tres familias, tres usos

| Tipo | Cuándo la usamos | Ejemplos |
|---|---|---|
| **Excepciones propias del negocio** (`extends Exception`, *checked*) | Situaciones del negocio que el usuario tiene que ver y que quien llama **está obligado** a manejar. Al ser checked, el compilador no deja ignorarlas. | `SeleccionInvalidaException` (regla de venta o asiento ocupado), `ReservaVencidaException` (pasaron los 5 minutos), `DevolucionNoPermitidaException` (faltan menos de 48 hs o no está pagada), `AutenticacionException` (email o contraseña incorrectos) |
| `IllegalArgumentException` (*unchecked*, de Java) | Datos inválidos: nombre vacío, DNI mal formado, código promocional inexistente, sede repetida. | Constructores de `Sede`, `Sector`, `Cliente`, `Cuenta`…, `Boleteria.agregarSede`, `Boleteria.aplicarCodigoPromocional` |
| `IllegalStateException` (*unchecked*, de Java) | Operación que no corresponde al estado actual del objeto. | `Compra.verificarReservada` (agregar algo a una compra ya pagada), `Boleteria.verificarSinFunciones` (dar de baja una sede con funciones), `PaqueteVIP.reservarCupo` sin cupo |
| `IOException` (*checked*, de Java) | Error al leer o escribir `datos/`. | Repositorios, `FacturaArchivo` |

**Por qué las propias son checked.** Porque son parte del contrato del método: `confirmarCompra` *puede*
fallar porque la reserva venció y quien la llama tiene que decidir qué hacer (mostrar el mensaje y
limpiar la pantalla). Las de datos inválidos son unchecked porque pueden salir de cualquier
constructor y atraparlas una por una llenaría el código de `throws`.

**Por qué no se usa `Exception` genérica.** Cada excepción propia tiene nombre y mensaje del negocio. La
pantalla no mira el texto para decidir: mira el tipo (`catch (ReservaVencidaException ex)` hace algo
distinto que `catch (SeleccionInvalidaException ex)`).

### Dónde está cada try/catch y qué hace con el error

| Lugar | Atrapa | Qué hace |
|---|---|---|
| `vista/PanelAltaSede.guardar()` | `IllegalArgumentException`, `IOException` | Muestra el mensaje en la barra de aviso. Dato inválido y error de archivo se muestran distinto. |
| `vista/PanelModificacionSede` | `IllegalArgumentException`, `IllegalStateException`, `IOException` | Ídem; el `IllegalState` es "la sede tiene funciones programadas". |
| `vista/PanelBajaSede.darDeBaja()` | `IllegalStateException`, `IOException` | Pide confirmación antes (`JOptionPane`) y muestra por qué no se pudo. |
| `vista/PanelVenta.reservar()` | `SeleccionInvalidaException`, `IllegalStateException` | Muestra el mensaje (asiento suelto, preventa, sin cupo VIP). La compra ya fue cancelada por el controlador. |
| `vista/PanelVenta.aplicarPromocion()` | `IllegalArgumentException` | "El código promocional X no existe". |
| `vista/PanelVenta.pagar()` | `ReservaVencidaException` | Limpia la pantalla y avisa que hay que volver a elegir. |
| `vista/BoleteriaApp.main` | `IOException` | Si `datos/` está corrupto, muestra un diálogo y no abre la ventana. |
| `controlador/VentaController.reservar()` | `SeleccionInvalidaException`, `IllegalStateException` | **Cancela la compra** (libera asientos y cupo VIP) y **relanza**: el controlador deja el modelo consistente, la vista muestra. |
| `modelo/Boleteria.confirmarCompra()` | `IOException` al guardar la factura | La venta **ya está confirmada**; si falla el archivo se avisa por consola pero no se pierde la compra. Decisión de negocio: antes perder un comprobante en disco que una venta. |
| `modelo/persistencia/SedeRepositorioArchivo.cargarTodos()` | `RuntimeException` por línea | La envuelve en una `IOException` con el número de registro: "sectores.txt, registro 7 inválido: …". |
| `modelo/ventas/Cuenta.hashear()` | `NoSuchAlgorithmException` | La convierte en `IllegalStateException`: SHA-256 siempre existe en Java; si no está, es un error de entorno, no del usuario. |
| `DemoFlujoCompra` | Las cuatro propias + `IllegalArgumentException` | Muestra por consola cada caso de error del flujo. |

**Regla que seguimos:** el modelo lanza, el controlador deja todo consistente, la vista muestra.
Ninguna pantalla decide qué es válido.

---

## 8. El flujo de compra y la reserva de 5 minutos

### Por qué hay reserva

Entre que el cliente elige asientos y paga pasan minutos. Sin reserva, dos operadores podrían vender el
mismo asiento. Con reserva permanente, un cliente indeciso bloquearía asientos para siempre. Cinco
minutos (`Compra.MINUTOS_RESERVA`) es lo que hacen las ticketeras reales.

### Cómo funciona (ver [secuencia](diagramas/flujo-de-compra/secuencia_compra.png))

1. **Elegir.** La pantalla pide `funcionesDisponibles()` y, por función, los sectores de su sede. Antes de
   dibujar el mapa llama a `liberarReservasVencidas()`: así un asiento cuya reserva venció aparece libre.
2. **Reservar.** `VentaController.reservar` → `Boleteria.iniciarCompra` (crea la `Compra` con
   `vencimiento = ahora + 5'` y le aplica los cargos vigentes) → `Boleteria.reservarAsientos` (verifica
   disponibilidad, valida **todas** las reglas y recién entonces `compra.agregarEntrada` por asiento).
   Si algo falla, no queda ninguna entrada creada: la validación va antes de la creación.
3. **Esperar.** Un `Timer` de Swing llama a `tic()` cada segundo; la vista pregunta al controlador
   `segundosRestantes()` y `verificarVencimiento()`. Si venció, el controlador vence la compra (los
   asientos se liberan) y la pantalla vuelve al inicio.
4. **Pagar.** `Boleteria.confirmarCompra` crea el `Pago`, llama a `compra.confirmar(pago, ahora)` (que
   vuelve a chequear el vencimiento: la pantalla pudo quedar abierta), pasa las entradas a `PAGADA`,
   registra la compra en el cliente, crea la `Factura` y la guarda.

### Por qué el modelo no tiene un hilo ni un reloj

`Compra` no usa `LocalDateTime.now()`: recibe `ahora` por parámetro en `estaVencida(ahora)` y
`confirmar(pago, ahora)`. El vencimiento es **perezoso**: nadie "despierta" a los 5 minutos; cada vez que
alguien consulta disponibilidad o confirma, se chequea la hora. Ventajas: no hay hilos ni condiciones de
carrera, y se puede probar "pasaron 6 minutos" pasando `ahora.plusMinutes(6)`, que es exactamente lo que
hace `DemoFlujoCompra` y el link "Demo: adelantar el reloj" de la pantalla (que suma minutos al
`desfaseReloj` del controlador).

### Estados ([diagrama](diagramas/flujo-de-compra/estados_compra_entrada.png))

- **Compra:** `RESERVADA` → `CONFIRMADA` (pagó a tiempo) · `VENCIDA` (pasaron 5') · `CANCELADA` (el
  usuario canceló). Vencer y cancelar hacen lo mismo con los asientos (`liberar`) pero quedan registrados
  con distinto estado para saber qué pasó.
- **Entrada:** `RESERVADA` → `PAGADA` → `DEVUELTA`; o `RESERVADA` → `ANULADA` (la compra venció o se
  canceló). `estaActiva()` = reservada o pagada = ocupa lugar. Esa es la única pregunta que hace `Funcion`
  para saber si un asiento está libre.
- **Función:** `PROGRAMADA` ⇄ `AGOTADA` según queden lugares; `CANCELADA` y `FINALIZADA` son finales.

### Devolución

`Boleteria.devolverEntrada(codigo, motivo, ahora)` busca la entrada entre las compras confirmadas,
pregunta `entrada.puedeDevolverse(ahora)` (pagada y 48 hs o más antes, o función cancelada), crea la
`Devolucion` y pasa la entrada a `DEVUELTA`, con lo que el asiento vuelve a estar a la venta. El reintegro
es el precio de la entrada sin cargos. La lógica está completa y probada en la demo; la pantalla es de la
entrega final.

---

## 9. Persistencia en archivos

**Por qué archivos de texto.** La consigna pide "primer avance de persistencia" y archivos de texto son
suficientes, legibles y versionables. El dominio no sabe que son archivos: habla con `Repositorio<T>`.

| Archivo | Formato | Decisiones |
|---|---|---|
| `datos/sedes.txt` | `nombre\|direccion` | Una línea por sede. |
| `datos/sectores.txt` | `sede\|NUM\|codigo\|nombre\|piso\|distancia\|filasXasientos` o `sede\|GEN\|codigo\|nombre\|piso\|distancia\|capacidad` | Los sectores se guardan aparte porque son muchos por sede y tienen más campos. Un numerado rectangular se guarda como `8x16` (compacto); si no es rectangular, se listan los asientos. |
| `datos/clientes.txt` | `dni\|nombre\|email\|hashSHA256` | Nunca la contraseña. Un atacante con el archivo no puede entrar. |
| `datos/facturas/FAC-0001-00000001.txt` | Texto del comprobante | Un archivo por venta, con el texto que ve el cliente. |

**Por qué `|` como separador** y por qué los nombres no pueden contenerlo: es el separador de campos;
`Sede`, `Sector` y `Cliente` lo rechazan al construirse para que el archivo nunca quede roto. Las líneas
que empiezan con `#` son comentarios (encabezados).

**Cuándo se guarda.** Después de cada operación del ABM (`SedeController` llama a
`boleteria.guardarSedes()`), al registrar cuentas de ejemplo y al confirmar una compra (factura). Si la
carpeta `datos/` no existe, `DatosIniciales` la crea con datos de ejemplo.

**Qué no se persiste todavía.** Eventos y funciones (se crean en memoria en `DatosIniciales`) y las
compras. Queda para la entrega final.

---

## 10. Relaciones del diagrama de clases

Usamos las definiciones del apunte de la cátedra ("Relaciones entre clases"):

| Relación | Definición (apunte) | En el TPO | Por qué esa y no otra |
|---|---|---|---|
| **Composición** (◆) | Las partes solo existen asociadas al compuesto | `Sede` ◆ `Sector` · `SectorNumerado` ◆ `Asiento` · `Evento` ◆ `Funcion` · `Funcion` ◆ `Preventa` (0..1) · `Compra` ◆ `Entrada` · `Compra` ◆ `Pago` (0..1) · `Cliente` ◆ `Cuenta` | Un sector sin sede no tiene sentido; una entrada sin compra tampoco. Las partes se crean desde el todo (Creador). |
| **Agregación** (◇) | Las partes pueden formar parte de distintos agregados | `Recital` ◇ `Artista` · `Compra` ◇ `PaqueteVIP` · `Compra` ◇ `Cargo` | El mismo artista toca en varios recitales; el mismo cargo del 12 % se aplica a todas las compras; el paquete existe antes de la compra. |
| **Asociación** (→) | Conexión estructural; navegable en un sentido | `Funcion` → `Sede` · `Entrada` → `Sector`, `Asiento` · `Cliente` — `Compra` (bidireccional) · `Factura` → `Compra` · `Devolucion` → `Entrada` · `Boleteria` → sus colecciones | La función "se realiza en" una sede pero no la contiene. Cliente–Compra es bidireccional porque la compra sabe su cliente y el cliente su historial. |
| **Dependencia** (- - >) | Relación de uso | `ReglaDeVenta` ··> `SeleccionInvalidaException` · `SectorFactory` ··> `Sector` · `Boleteria` ··> excepciones | Se usan en un método, no se guardan como atributo. |
| **Herencia** (△) | Generalización/especialización | `Sector` ◁ `SectorNumerado`, `SectorGeneral` · `Evento` ◁ `Recital` | "Es un": un sector general **es un** sector. |
| **Realización** (△ punteada) | Implementar una interfaz | `ReglaDeVenta` ◁· `NoDejarAsientoSuelto`, `RequiereCodigoPreventa` · `Repositorio` ◁· los repositorios de archivo | Contrato sin estado compartido. |

**Multiplicidades que vale la pena saber:** `Sede 1 — 1..* Sector` (una sede necesita al menos un
sector; lo valida `Boleteria.agregarSede`), `Compra 1 — 1..* Entrada` (no se confirma una compra sin
entradas), `Funcion 1 — 0..1 Preventa`, `Compra * — 0..1 Promocion`.

---

## 11. Decisiones de implementación en Java

Cosas del código que conviene poder justificar porque son temas de la materia:

- **Encapsulamiento.** Atributos `private` (o `protected` en las clases abstractas para que las hijas
  los usen), `final` cuando no cambian después del constructor (`codigo`, `dni`, `nroCompra`). Los
  getters de colecciones devuelven `Collections.unmodifiableList(...)`: se puede leer, no modificar.
  Validación en el constructor: un objeto inválido no llega a existir.
- **Colecciones (clase 11).** `ArrayList` para listas con orden de inserción. `LinkedHashSet<Asiento>`
  para la selección en el mapa: sin repetidos y en el orden en que se marcaron. `HashSet` en
  `NoDejarAsientoSuelto` para comparar ocupados rápido. `Optional` en `buscarSede`, `buscarAsiento`,
  `buscarEntrada` para no devolver `null`. Streams para filtrar y sumar (`calcularSubtotal`, `buscarFunciones`).
- **`equals` y `hashCode`.** Definidos en `Asiento` (fila + número), `Sector` (código) y `Artista`
  (nombre sin mayúsculas). Sin eso, `set.contains(asiento)` o `sectores.contains(sector)` compararían
  referencias y dos `new Asiento("A", 1)` serían distintos.
- **Enums con comportamiento.** `EstadoEntrada.estaActiva()`, `MedioPago` y `TipoSector` con descripción
  y `toString()` para mostrarlos en los combos sin un `switch` en la vista.
- **Clases internas (clase 10).** `PanelVenta.BotonAsiento` es una clase interna (no estática) porque
  necesita la selección y el aviso del panel; `PanelVenta.Escenario` y `VentanaPrincipal.ItemMenu` son
  anidadas estáticas porque no usan nada de la instancia. Clases anónimas para el renderer del combo y el
  ícono de la leyenda; lambdas para los listeners.
- **Sobrecarga y sobrescritura (clase 8).** `Boleteria.reservarAsientos` está **sobrecargado**: una versión
  recibe `SectorNumerado` y lista de asientos, la otra `SectorGeneral` y cantidad. `SectorGeneral`
  **sobrescribe** `calcularMultiplicador()` y llama a `super`.
- **Pattern matching.** `if (sector instanceof SectorNumerado numerado)` evita el cast aparte (Java 17).
- **`static`.** `Compra.secuencia` y `Factura.secuencia` numeran correlativamente; `Compra.MINUTOS_RESERVA`
  y `Devolucion.HORAS_LIMITE` son constantes de negocio con nombre, no números sueltos.
- **Fecha como parámetro.** El modelo nunca llama a `LocalDateTime.now()`; lo recibe. Lo único que mira el
  reloj real es `VentaController.ahora()`.

---

## 12. Qué sacamos de la entrega 1 y por qué

| Estaba en la entrega 1 | Qué pasó | Por qué |
|---|---|---|
| `Estacionamiento`, interfaz `Adicional` | Se quitó | La devolución pidió centrar el proyecto en la venta de entradas. El único adicional que quedó es `PaqueteVIP`, y sin una interfaz para un solo caso (YAGNI). |
| `PoliticaPrecio`, `PrecioEstandar`, `PrecioDinamico` | Se quitó | El precio dinámico por ocupación era lo más complejo y lo menos central. El precio quedó en `Funcion.calcularPrecio(sector)`. Si vuelve, se agrega como Strategy igual que las reglas. |
| `ExperienciaInmersiva` | Se quitó | Un segundo tipo de evento sin funcionalidad propia. `Evento` sigue abstracta para agregarlo cuando sume. |
| `TipoCargo` (servicio / impuesto) | Se quitó | Un cargo es un nombre y un porcentaje; el tipo no cambiaba ningún cálculo. |
| `EstadoEntrada.USADA` | Pasó a `DEVUELTA` | El control de acceso al show está fuera de alcance; la devolución sí es parte del flujo. |
| — | Se agregó: reserva de 5 min, `EstadoCompra`, `Pago`, `MedioPago`, `Factura`, `Devolucion`, `Promocion`, `Cuenta`, `SectorFactory`, `Repositorio`, `ReservaVencidaException`, `DevolucionNoPermitidaException`, `AutenticacionException` | Es lo que pidió la devolución: "armar el flujo de compra completo como un caso real". |

El diagrama v3 muestra el diseño acotado y el v4 es el que coincide con el código.

---

## 13. Limitaciones conocidas y lo que falta

Mejor decirlo que esperar a que lo encuentren:

- **Sin tests automatizados todavía.** `DemoFlujoCompra` recorre el flujo completo con sus casos de
  error y sirve como prueba de integración manual, pero no hay JUnit. Es lo primero a agregar: el modelo
  está preparado (fechas por parámetro, repositorios detrás de interfaz).
- **Numeración de facturas.** `Factura.secuencia` es `static` y arranca en 1 en cada ejecución, así que
  al reiniciar la app el archivo `FAC-0001-00000001.txt` se vuelve a escribir. Hay que persistir la
  secuencia junto con las compras.
- **Eventos, funciones y compras no se persisten.** Se crean en memoria en `DatosIniciales`.
- **Pantalla de devoluciones y los dialogs** quedan para la entrega final (la lógica ya está).
- **Inversión de dependencias incompleta en `Boleteria`.** Crea sus repositorios con `new`. Para tests
  con repositorio en memoria habría que pasarlos por constructor o por un método de configuración.
- **`PanelInicio` lee `Boleteria` directo** para las estadísticas de la portada. Es solo lectura, pero
  para ser estrictos con MVC podría pasar por un controlador.
- **La app debe ejecutarse desde `boleteria/`** porque `datos/` es una ruta relativa.

---

## 14. Preguntas probables en la defensa

**¿Por qué `Boleteria` está en `modelo` si es "el controlador"?**
Porque es el Controlador de GRASP (fachada que recibe las operaciones del sistema y delega), no el
controlador de MVC. En MVC es modelo: los controladores son `SedeController` y `VentaController`, que
están entre Swing y la boletería.

**¿Qué pasa si quiero agregar una regla "máximo 10 entradas por compra"?**
Una clase `MaximoEntradasPorCompra implements ReglaDeVenta` con el `validar`, y una línea en
`DatosIniciales`: `boleteria.agregarRegla(new MaximoEntradasPorCompra(10))`. No se toca `Boleteria` ni
ninguna pantalla. (Hoy ese límite está solo en la vista, como tope del mapa.)

**¿Y un tipo de sector nuevo, por ejemplo "palco"?**
Subclase de `Sector` con su `getCapacidad()` y su multiplicador, un valor en `TipoSector`, un `case` en
`SectorFactory` y el formato en `SedeRepositorioArchivo`. Es el único cambio que toca más de un archivo,
y está localizado en la creación y la persistencia.

**¿Por qué el asiento no sabe si está ocupado?**
Porque el mismo asiento físico se vende por separado en cada función. Quien tiene las entradas es la
`Funcion`, así que es la experta en disponibilidad. Si `Asiento` tuviera un booleano `ocupado`, habría
que resetearlo por función y se rompería al vender dos funciones en la misma sede.

**¿Por qué las excepciones propias son checked?**
Porque son resultados posibles del negocio que quien llama tiene que manejar sí o sí: la pantalla tiene
que mostrar "la reserva venció". Un `RuntimeException` se puede ignorar por descuido.

**¿Por qué la promoción no descuenta el paquete VIP?**
Decisión de negocio: la promo (banco, club de fans) es sobre las entradas. Está en un solo lugar,
`Promocion.calcularDescuento`, que usa `calcularSubtotalEntradas()`. Cambiarlo es una línea.

**¿Por qué el reintegro no incluye los cargos?**
Porque el cargo por servicio paga la operación, que ya se hizo. Está en `Devolucion.calcularReintegro()`
y se avisa en la factura.

**¿Dónde está el polimorfismo?**
`Sector.calcularMultiplicador` / `getCapacidad` (numerado vs general), `ReglaDeVenta.validar` (cada regla
valida distinto), `Evento.getTipo`. Y la prueba de que se usa: `Boleteria` y `Funcion` trabajan con
`Sector` y `ReglaDeVenta`, nunca con las clases concretas, salvo el `switch` de la fábrica.

**¿Por qué Singleton y no pasar la boletería por parámetro?**
Hay una sola boletería y la necesitan la app Swing, la demo y `DatosIniciales`. El singleton garantiza
que sea la misma. Pero las clases que la usan la reciben por constructor, así que no están atadas a
`getInstancia()`.

**¿Cómo probarían que la reserva vence sin esperar 5 minutos?**
Pasando `ahora.plusMinutes(6)` a `confirmarCompra`, como hace la demo. El modelo no mira el reloj.

**¿Qué pasa si falla el disco al guardar la factura?**
La compra ya está confirmada y queda en memoria; se avisa por consola. Preferimos no perder una venta
porque falló un archivo.

**¿Por qué dos archivos para sedes y sectores?**
Son dos entidades con campos distintos y hay muchos sectores por sede. Un archivo por entidad es más
simple de leer y de escribir que un formato anidado.

**¿Qué cambiarían si tuvieran más tiempo?**
Tests JUnit del modelo, persistir compras y la secuencia de facturas, inyectar los repositorios en
`Boleteria`, y la pantalla de devoluciones.
