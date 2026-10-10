# MEJORAS: backlog y análisis de implementabilidad

Proyecto: Pizzería Bella Napoli (entorno docente DAW/DAM, IES La Mola)
Versión del documento: 0.2 · Fecha: 2026-10-10

## 1. Para qué sirve este documento

Aquí se **proponen** las mejoras del proyecto y se **decide, en momentos concretos**, cuáles se hacen, cuándo y cómo. Proponer es gratis: cualquier idea entra como ficha. Implementar solo se hace tras un análisis de implementabilidad (sección 5).

Sirve también como material de clase: cada mejora indica qué asignatura la aprovecha y qué se aprende al hacerla.

## 2. Ciclo de vida de una mejora

```
Idea → En análisis → Planificada → En curso → Hecha
                  ↘ Aplazada (con motivo y fecha de revisión)
                  ↘ Descartada (con motivo)
```

Reglas:

1. Una mejora = una ficha con ID (`M-01`, `M-02`...). Los IDs no se reutilizan.
2. No se empieza ninguna mejora sin análisis, salvo las de prioridad A y tamaño S.
3. Una rama por mejora, con commits pequeños y en español.
4. Los cambios de backend llevan **tests** y van en rama aparte de la app.
5. Si cambia un endpoint, se actualiza `openapi.yaml` en el mismo cambio.
6. Al fusionar, se actualiza el estado en este documento.

## 3. Escalas

**Prioridad**

| Valor | Significado |
|---|---|
| A | Puede romper una demo o hacer perder dinero |
| B | Incoherencia visible para el personal o el cliente |
| C | Mejora o ejercicio de clase |

**Tamaño**

| Valor | Significado |
|---|---|
| S | Menos de 30 líneas de cambio |
| M | Una tarea de una hora a una sesión |
| L | Una fase completa, con varias piezas |

**Área:** backend, frontend-web, PWA (frontend-qr-app), app (Android), BD, infraestructura, guías.

## 4. Momentos de análisis

| Momento | Qué se hace | Resultado |
|---|---|---|
| Antes de cada sprint o fase | Se revisa el backlog y se eligen las mejoras de la fase | Lista de mejoras "Planificadas" |
| Antes de abrir la rama de una mejora | Se rellena la plantilla de la sección 5 | Decisión: adelante, adelante con cambios, aplazar o descartar |
| Después de cada demo o prueba con alumnos | Se anotan fallos y fricciones como ideas nuevas | Fichas nuevas y prioridades revisadas |
| Antes de cada curso | Se mapean las mejoras a asignaturas y resultados de aprendizaje | Plan de prácticas |
| Cuando cambia el entorno (Learner Lab, versiones, bloqueos de red) | Se reevalúan las mejoras que dependían de ello | Estados actualizados |

## 5. Plantilla de análisis de implementabilidad

Responde cada punto con hechos. Si no lo sabes, escribe **"Por verificar"** y di cómo se verificará. No se supone.

1. **Problema y valor:** qué resuelve, para quién y qué pasa si no se hace.
2. **Alcance mínimo viable:** lo más pequeño que ya aporta valor.
3. **Piezas que toca:** backend, frontend-web, PWA, app, BD, infraestructura, `openapi.yaml`, guías.
4. **Compatibilidad:** ¿rompe algo que funciona hoy? ¿Siguen funcionando la web, la PWA y la app ya instalada?
5. **Datos:** ¿cambia el esquema de RDS? ¿Hay migración reversible? ¿Hace falta copia previa (snapshot)?
6. **Entorno:** ¿necesita permisos o servicios que el Learner Lab puede no ofrecer? Se comprueba antes de diseñar.
7. **Datos personales:** ¿trata teléfonos, direcciones o correos? Puede haber menores. Se consulta la política del centro. Este documento no es asesoramiento legal.
8. **Riesgo para la demo:** qué puede fallar delante de un examinador y cómo se detecta antes.
9. **Pruebas:** cómo se demuestra que funciona (test automático o lista de comprobación manual).
10. **Coste:** dinero, cuota de agentes y tiempo de clase.
11. **Valor didáctico:** asignatura, resultados de aprendizaje y preguntas de entrevista que permite responder.
12. **Dependencias:** qué otras mejoras necesita o desbloquea.

**Decisión:** Adelante · Adelante con cambios · Aplazar · Descartar. Con fecha y motivo.

## 6. Prompt para pedir el análisis a un agente (solo lectura)

Se lanza en una conversación nueva, con el modelo más capaz y en modo plan, sin crear rama.

```
Tarea: análisis de implementabilidad de la mejora M-XX ("título") de MEJORAS.md.
Solo lectura: no modifiques archivos, no hagas commits, no ejecutes docker, curl ni
gradlew. No leas .env ni los CSV. Lee MEJORAS.md (secciones 3 y 5), CLAUDE.md,
openapi.yaml, AUDITORIA.md y el código de las piezas afectadas.

Rellena la plantilla de la sección 5 punto por punto con archivo y línea cuando
corresponda. Marca "Por verificar" lo que dependa de servicios externos o del
entorno y propón cómo comprobarlo. Termina con: alcance mínimo propuesto, plan de
tareas en fases pequeñas (una rama y un commit por tarea), riesgos, tamaño (S/M/L)
y tu recomendación (adelante, adelante con cambios, aplazar o descartar).
```

## 7. Backlog

| ID | Mejora | Área | Prior. | Tam. | Estado |
|---|---|---|---|---|---|
| M-01 | Código de seguimiento aleatorio en lugar del número de pedido | backend, web, PWA, app | B | L | Idea |
| M-02 | La cocina no muestra pedidos con tarjeta sin pagar | frontend-web | A | S | Hecha (pendiente de fusionar) |
| M-03 | Confirmación del personal antes de cocinar domicilios en efectivo | frontend-web | B | M | Idea |
| M-04 | Verificación por teléfono con código (modo laboratorio) | backend, BD, web, PWA, app | C | L | Idea |
| M-05 | Proveedor real de SMS y límites antiabuso | backend, infraestructura | C | M | Idea (depende de M-04) |
| M-06 | Reconciliación de pagos en el servidor | backend | A | M | Idea |
| M-07 | Caducidad de pagos abandonados | backend | B | M | Idea |
| M-08 | Cancelar con motivo, ocultar cancelados y limpieza controlada de pruebas | web, backend, BD | B | M | Idea |
| M-09 | Etiqueta única de situación del pedido en Caja y Cocina | frontend-web | B | M | Idea |
| M-10 | Tabla de eventos del pedido (trazabilidad) | BD, backend | C | L | Idea |
| M-11 | Marca "a reembolsar" y reembolso desde Stripe | backend, web | C | L | Idea |
| M-12 | Seguimiento web: "Pendiente de pago" en lugar de "Recibido" | frontend-web | B | S | Idea |
| M-13 | Webhook y confirmar-sesion endurecidos | backend | A | M | Idea |
| M-14 | Retorno de Stripe directo a la app | backend, app | C | M | Idea |
| M-15 | Lector de QR de mesa en la app | app | C | M | Idea |
| M-16 | Fuentes de la web en la app (licencia por verificar) | app | C | S | Idea |
| M-17 | Aviso "DEMO EDUCATIVA, sin validez fiscal" en el ticket | frontend-web | B | S | Planificada (decidido: ticket didáctico) |
| M-18 | Aviso de usar datos ficticios (web, PWA, app) e instrucciones de demo | web, PWA, app, guías | A | S | Idea |
| M-19 | Carta definitiva: fotos correctas, ingredientes y alérgenos sembrados | BD | B | M | Idea |
| M-20 | Migrar frontend-web a React por pantallas | frontend-web | C | L | Idea |
| M-21 | "Mis pedidos" guardado en la app | app | C | M | Idea |
| M-22 | Comprobación de arranque tras reiniciar el laboratorio | infraestructura | A | M | Idea |
| M-23 | Plan de contingencia ante bloqueos de red | infraestructura, guías | A | M | Idea |
| M-24 | Cuentas y roles (cliente, cocina, administración) | backend, BD, clientes | C | L | Idea |
| M-25 | Verificación de la dirección de reparto | backend, web | C | M | Idea |
| M-26 | Cobros coherentes: rechazo (409) de cobros sobre pedidos pagados o cancelados, descuento incluido en Stripe, una sola sesión viva por pedido | backend, frontend-web | A | M | Idea |
| M-27 | Máquina de estados del pedido y regla de mesa (se libera al servir y pagar) | backend | B | M | Idea |
| M-28 | Pulido del seguimiento, Cocina, mostrador y PWA (etiquetas, columnas y mensajes) | frontend-web, PWA | B | M | Idea |
| M-29 | Recordar varios pedidos y limitar el sondeo a Stripe | frontend-web | B | S | Idea |
| M-30 | Listados por fecha y con índices en lugar de todo el histórico | backend, BD, frontend-web | B | M | Idea |
| M-31 | Zona horaria y fechas coherentes | BD, frontend-web | C | S | Idea |
| M-32 | Histórico y arqueo: desglose por método de pago, caja y ticket medio | frontend-web | C | S | Idea |
| M-33 | Mesas: recarga, estado de cuenta pedida, renumerar y validar `?mesa=` | frontend-web, backend | C | S | Idea |
| M-34 | Descuentos como dato explícito | BD, backend | C | M | Idea |
| M-35 | Tests de caracterización y CI del backend (fase 0, requisito de las fases 1 y 3) | backend, CI | A | M | Planificada |

### Hoja de ruta de la v1.0 (no son mejoras, son pasos pendientes)

1. Probar la app en el teléfono y fusionar `app-sprint-d`.
2. Fusionar el filtro de cocina (M-02) y actualizar la EC2.
3. Seguridad manual: cambiar las contraseñas de RDS y de DbGate, y proteger o apagar `/dbgate/`.
4. Servir el APK desde la EC2 con su código QR.
5. Primera versión firmada `v1.0.0`.
6. Guías ejecutadas desde cero en un laboratorio limpio.

## 8. Fichas analizadas

### M-01 · Código de seguimiento aleatorio

- **Problema:** cualquiera puede consultar pedidos probando números consecutivos, y ve nombre, teléfono y dirección. Comprobado en ventana de incógnito.
- **Propuesta mínima:** al crear el pedido, el servidor genera un código aleatorio largo y lo devuelve. El seguimiento público exige ese código en lugar del número. Opción intermedia: que el endpoint público devuelva solo estado, líneas y total, sin datos personales.
- **Piezas:** backend, web, PWA y app. Cambia el contrato.
- **Por verificar:** qué pantallas usan hoy los campos personales de `GET /api/pedidos/{id}`.
- **Valor didáctico:** control de acceso roto (OWASP), diseño de identificadores, migraciones.

### M-03 · Confirmación del personal para domicilios en efectivo

- **Problema:** un domicilio falso se cocina y se reparte sin que nadie lo valide.
- **Propuesta mínima:** los domicilios en efectivo no aparecen en cocina hasta que el personal los confirma, como hoy ocurre con los de tarjeta sin pagar. Primera versión solo en `frontend-web`, sin cambiar la base de datos.
- **Por verificar:** si hace falta un estado nuevo o basta una marca en un campo existente.

### M-04 y M-05 · Verificación por teléfono

- **Problema:** se quiere reducir pedidos falsos sin obligar a crear una cuenta con contraseña.
- **Propuesta:** código de 6 cifras enviado al teléfono, guardado con hash y con caducidad de unos 5 minutos, con número limitado de intentos. Se pide solo en domicilio y recoger.
- **Diseño clave:** el envío del código va detrás de una interfaz intercambiable.
  - **Modo laboratorio (por defecto):** no envía SMS; el código aparece en el registro del servidor o en pantalla con aviso de demo. Coste cero.
  - **Modo real (M-05, opcional):** conecta con un proveedor de SMS.
- **Por verificar:**
  - Si el Learner Lab permite enviar SMS con el servicio de AWS (suele tener restricciones) y si las cuentas de prueba solo envían a números verificados.
  - Coste por mensaje con 30 alumnos.
- **Riesgos del modo real:** abuso del propio sistema (pedir miles de códigos a números de pago especial). Hacen falta límites por teléfono y por IP, espera entre reenvíos y, si es posible, aceptar solo números españoles.
- **Datos personales:** el teléfono lo es. Se consulta la política del centro. En la demo, datos ficticios.
- **Valor didáctico:** seguridad, bases de datos, arquitectura (aislar una dependencia externa) y, para DAM, pantalla de verificación con temporizador.

### M-06 y M-13 · Reconciliación de pagos y webhook endurecido

- **Problema:** si el cliente paga, el aviso de Stripe falla y cierra la pestaña, el pedido queda como pendiente de pago, oculto en cocina (M-02) y sin aviso. Hoy solo se recupera mientras el cliente mantiene abierto el seguimiento. Además, el webhook acepta eventos sin firma si falta el secreto, no comprueba `payment_status` y no deduplica por `event.id`, y `confirmar-sesion` marca el pedido que llega en el cuerpo aunque la sesión sea de otro (AUDITORIA.md).
- **Propuesta mínima:** el servidor revisa periódicamente los pedidos con sesión de pago abierta y consulta a Stripe usando **solo la sesión guardada de ese pedido**. En Caja, botón "Comprobar pago".
- **Piezas:** backend con tests y rama aparte; `frontend-web` para el botón.
- **Por verificar:** el límite de llamadas a Stripe con muchos alumnos y el esquema de la tabla de pedidos.

### M-23 · Contingencia ante bloqueos de red

- **Problema observado:** el 2026-10-10, por la tarde, el servidor respondía desde la EC2 pero ni el PC ni el móvil (con datos) en España obtenían respuesta. Con VPN funcionaba. La prensa tecnológica documenta bloqueos de direcciones IP de Cloudflare en horas de partido, y es la hipótesis más probable, aunque no está confirmada para ese día.
- **Medidas mínimas:** comprobar el acceso 15 minutos antes de cualquier demo, desde la misma red y dispositivo; no fijar presentaciones en horas de partido; tener un plan B (otro operador o VPN).
- **Por analizar:** alternativas que no dependan del proxy de Cloudflare y qué se pierde con ellas.

## 9. Escalera de identidad y acceso

| Nivel | Qué incluye |
|---|---|
| 0 (actual) | Sin cuentas. Pedidos consultables por número |
| 1 | Código de seguimiento aleatorio (M-01) |
| 2 | Verificación por teléfono en modo laboratorio, solo en domicilio y recoger (M-04) |
| 3 | Proveedor real de SMS y límites (M-05), roles de personal y verificación de dirección (M-24, M-25) |

En mesa (QR) no se pide registro: el cliente está presente y el personal lo ve.

## 10. Decisiones aceptadas para este curso

- La API no autentica: cualquiera que conozca un número de pedido puede consultarlo y, por extensión, intentar modificarlo.
- Las credenciales del personal pueden verse en la interfaz (entorno docente).
- Stripe funciona solo en modo de prueba.
- Nadie debe introducir datos personales reales en la demo.
- Los pedidos de prueba se cancelan al terminar cada sesión.
- Cada alumno tiene su propia base de datos, que se puede reinicializar. Las pruebas del backend pueden reiniciar su esquema.

## 11. Revisión funcional del ciclo del pedido (2026-10-10)

Un agente revisó el código en modo solo lectura y devolvió 29 hallazgos con archivo y línea. **No están probados en ejecución.** Esta tabla los agrupa por mejora y por fase.

| Hallazgos | Resumen | Mejora | Fase |
|---|---|---|---|
| 1 | Pagado con el webhook caído: el pedido no llega nunca a Cocina (efecto del filtro M-02) | M-06 | 1 |
| 2, 3, 4, 5, 7 | Cobros incoherentes: varias sesiones vivas, efectivo más Stripe, descuento no cobrado, se pueden cobrar cancelados | M-26 | 1 |
| 14 | Ticket con año fijo, huecos y emitible de cualquier pedido | M-17 | 2 |
| 13, 15, 16, 17, 22, 28 | Etiquetas, columnas y mensajes incoherentes en seguimiento, Cocina, mostrador y PWA | M-28 | 2 |
| 20, 21 | Un solo pedido recordado y sondeo ilimitado a Stripe | M-29 | 2 |
| 6 | Cancelar un pedido pagado: sin reembolso y sigue contando como facturado | M-11, M-08 | 3 |
| 9 | Pedido de Stripe abandonado: pendiente para siempre | M-07 | 3 |
| 10, 11, 12 | Transiciones libres y mesa liberada por efectos secundarios | M-27 | 3 |
| 8 | Todas las pantallas descargan el histórico completo cada 5 segundos | M-30 | 4 |
| 23 | Fechas en UTC y columnas sin zona | M-31 | 4 |
| 25 | Descuento implícito que se pierde al reabrir un pedido | M-34 | 4 |
| 18, 24, 29 | Histórico, desglose por método y arqueo | M-32 | 5 |
| 19, 26, 27 | Mesas: recarga, renumerar y validar la mesa | M-33 | 5 |

### Fases

| Fase | Contenido | Dónde |
|---|---|---|
| 0 | M-35: tests de caracterización y CI del backend | backend, CI |
| 1 | M-06, M-26, M-13: dinero | backend, con tests |
| 2 | M-28, M-29, M-17, M-12: coherencia visible | frontend-web y PWA |
| 3 | M-27, M-07, M-11, M-08: reglas de estado | backend, con tests |
| 4 | M-30, M-31, M-34: rendimiento y datos | backend y BD |
| 5 | M-32, M-33: ejercicios de clase | para el curso |

La fase 0 va antes que la 1 y la 3, porque cambian código que mueve dinero. Todo cambio de backend va con tests y en rama aparte de la app.

**Matiz sobre el hallazgo 8:** se calculó pensando en un único servidor. Con una EC2 y una RDS por alumno, el problema real es la acumulación de pedidos de prueba. Pasa de A a B en los laboratorios individuales y sube a A si hay una instancia compartida para la demo.

### Dudas abiertas de la revisión

1. Estado real de los pedidos #116 y #120 (se deduce que #116 está pagado y cancelado).
2. Zona horaria de RDS y de la EC2.
3. Si el webhook llega de verdad a cada laboratorio (en el de pruebas funciona desde la entrega de las 18:03 del 2026-10-06).
4. Cuántos pedidos hay hoy en RDS.
5. Si la app Android llamará a `/cobro` o `/comanda`, y con qué `metodo_pago`.
6. Límite de peticiones de Stripe en modo de prueba.

## 12. Reglas de negocio decididas

- **Ticket didáctico.** Lleva el aviso "DEMO EDUCATIVA, sin validez fiscal" y no pretende ser una serie correlativa legal. *Por decidir:* si se bloquea emitir ticket de pedidos cancelados o sin pagar (hoy se puede).
- **Mesa.** Se libera cuando su último pedido está **servido o entregado y pagado**. *Por decidir:* qué ocurre al cancelar un pedido, y cómo se avisa de una mesa servida pero sin cobrar.
- **Pruebas del backend (propuesta, a confirmar).** Pueden reiniciar el esquema de la base de datos individual. Si el servidor no es local, exigen una variable explícita (`TEST_ALLOW_REMOTE=1`), para que no ocurra por accidente.

## 13. Registro de cambios

| Versión | Fecha | Cambio |
|---|---|---|
| 0.1 | 2026-10-10 | Creación, con 25 mejoras iniciales y 5 fichas analizadas |
| 0.2 | 2026-10-10 | Revisión funcional (29 hallazgos), mejoras M-26 a M-35, fases 0 a 5 y reglas de negocio decididas (ticket didáctico y liberación de la mesa) |
