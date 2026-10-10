# Proyecto: Pizzería (entorno docente DAW/DAM)

## Contexto

Backend Node/Express (ESM), PostgreSQL en AWS RDS, dos frontends web en
JavaScript sin framework (frontend-web y frontend-qr-app, servidos por Nginx),
Docker Compose en EC2, Nginx y Cloudflare Tunnel. AWS Academy Learner Lab.
La app Android (carpeta app/) es un cliente nuevo de la misma API.

## Documentación de referencia

- openapi.yaml: describe la API REAL actual del backend (snake_case, envoltorio
  `{success, data, message}`, importes NUMERIC como texto). Es la referencia para
  los clientes. Las extensiones `x-nota` marcan comportamientos problemáticos que
  no están corregidos.
- P01 a P08 (\*.md en la raíz): guías de despliegue, seguridad, RDS y Stripe. Respétalas.
- docs/AUDITORIA.md (la carpeta docs/ está en .gitignore) y WORKFLOW.md: estado
  actual y flujo de trabajo.

## Reglas

- Consultas SQL siempre parametrizadas.
- NUNCA leas, imprimas ni subas secretos (.env, claves de Stripe, RDS, Cloudflare).
  Trabaja solo con .env.example.
- No ejecutes despliegues, docker compose up/down ni te conectes a RDS o AWS
  sin que yo te lo pida.
- No leas los CSV de alumnos ni profesores (datos personales).
- Tareas pequeñas, un commit por tarea.
- Los cambios se hacen en ramas de trabajo, nunca en main. No hagas push sin que
  yo lo pida.

## Objetivos futuros (no implementado)

Estas reglas eran el diseño previsto de la API. El backend actual NO las cumple;
no las des por hechas ni las apliques al código existente salvo que se pida
expresamente:

- Importes en céntimos enteros y precios recalculados siempre por el servidor
  (hoy los importes son euros NUMERIC; el servidor recalcula los precios solo al
  crear el pedido).
- JWT: access 15 min + refresh 7 días rotatorio y revocable (hoy no hay
  autenticación).
- Pedido en `pendiente_pago` hasta que el webhook de Stripe confirme el pago, con
  cuerpo raw, firma verificada e idempotencia (hoy el estado inicial es
  `pendiente` y el webhook acepta eventos sin firma).
- Campos de la API en camelCase (hoy es snake_case).

## App Android (carpeta app/)

- Kotlin + Compose Multiplatform. Solo Android en la v1.
- Red con Ktor Client y kotlinx.serialization; inyección de dependencias con Koin.
- La URL del servidor se configura en tiempo de ejecución (AjustesServidor),
  nunca en el código.
- La API usa snake_case: mapéalo con `@SerialName` y deja los identificadores
  Kotlin en español sin tildes.
- Importes: tipo `Importe` (céntimos en `Long`), sin `Double` ni `BigDecimal`.
  Acepta el número o el texto que envía la API (`"12.50"` o `12.5`).
- `GET /api/health` no usa el envoltorio `{success, data}`: tiene su propia clase.
- El seguimiento del pedido se hace consultando `GET /api/pedidos/{id}`
  (`estado` y `estado_pago`); no existe `/api/pedidos/{id}/estado` con GET.
- No hay JDK ni Android SDK en este equipo: no ejecutes gradlew. Solo el CI de
  GitHub (.github/workflows/app.yml) verifica la compilación y los tests.
- Cambios pequeños. No cambies versiones ni añadas dependencias sin necesidad.

## Idioma y convenciones (código nuevo de la app)

- Comentarios, commits, mensajes de error/log y documentación en español.
- Identificadores del dominio en español, sin tildes ni ñ (crearPedido, estado_pago).
- Se mantienen en inglés: palabras clave, APIs de librerías, cabeceras HTTP y
  campos estándar de JWT.
- No renombres ni refactorices código existente ni campos de la API.
