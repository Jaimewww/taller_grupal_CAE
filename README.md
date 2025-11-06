# Centro de Atención al Estudiante (CAE)

**Proyecto de Unidad — Estructura de Datos y Control (SLL, Stack, Queue, Persistencia, Undo/Redo)**

---

## Tabla de contenidos
1. [Descripción](#descripción)
2. [Funcionalidades principales](#funcionalidades-principales)
3. [Requisitos y tecnologías](#requisitos-y-tecnologías)
4. [Instalación y ejecución](#instalación-y-ejecución)
5. [Estructura del proyecto — Clases principales](#estructura-del-proyecto---clases-principales)
6. [Persistencia y formatos sugeridos](#persistencia-y-formatos-sugeridos)
7. [Reportes y exportación](#reportes-y-exportación)
8. [Pruebas unitarias (clases a testear)](#pruebas-unitarias-clases-a-testear)
9. [Evidencias / Capturas](#evidencias--capturas)
10. [Decisiones de diseño y casos borde](#decisiones-de-diseño-y-casos-borde)
11. [Contacto](#contacto)

---

## Descripción
El sistema **CAE** es una aplicación de consola para gestionar trámites de estudiantes. Usa estructuras de datos implementadas desde cero y un patrón Command para soportar undo/redo. El objetivo es demostrar dominio de SLL, pilas, colas, persistencia y reglas de negocio para transiciones de estado.

---

## Funcionalidades principales
- Registrar y enrutar tickets (prioridad urgente vs normal).
- Atender el siguiente ticket respetando prioridad.
- Agregar notas a un ticket (historial en lista enlazada).
- Cambiar estado de tickets validado por una máquina de estados.
- Undo/Redo de acciones principales (alta, nota, cierre).
- Persistencia en disco y exportación de reportes.

---

## Requisitos y tecnologías
El proyecto se gestiona con **Apache Maven**.

* **Java (OpenJDK 11+ / 17+):** Entorno de ejecución y compilación.
* **Maven (3.6+):** Herramienta de gestión de dependencias y construcción.
* **JUnit 5 (Jupiter):** Framework utilizado para las pruebas unitarias.
* Herramientas: IDE a elección, Git para control de versiones.
* Carpeta base de persistencia: `data/` (configurable).

---

## Instalación y ejecución

El proyecto utiliza **Apache Maven** para la gestión de dependencias y el ciclo de vida de la construcción (*build*).

### Compilar el Proyecto
Clona el repositorio y usa el comando `mvn clean install` para compilar, resolver dependencias y generar el **JAR ejecutable** en el directorio `target/`.

```bash
mvn clean install
```

### Ejecutar (ejemplo)
Para ejecutar la aplicación compilada, usa el JAR generado:
```
java -jar target/cae-1.0-SNAPSHOT.jar
```
Alternativamente, puedes ejecutar directamente desde Maven:
```bash
mvn exec:java -Dexec.mainClass="Main"
```

---

## Estructura del proyecto — Clases principales

> A continuación se listan las clases tal como fueron definidas. Úsalas como guía para ubicar archivos y paquetes.

### Paquete `domain`
- `Ticket` — entidad principal (id, student, procedureType, state, noteHistory).
- `Note` — observación con timestamp.
- `TicketState` — enum de estados (EN_COLA, URGENTE, EN_ATENCION, PENDIENTE_DOCS, COMPLETADO).
- `ProcedureType` — enum de tipos de trámite.

### Paquete `structures`
- `Node<T>` — nodo genérico.
- `SimpleList<T>` — lista enlazada simple (pushFront, pushBack, find, remove, size).
- `Queue<T>` — cola FIFO (enqueue, dequeue, peek, remove específico).
- `Stack<T>` — pila LIFO (push, pop, peek).
- `AttentionQueue` — orquesta colas normal/urgente y mantiene attendedHistory.

### Paquete `persistence`
- `PersistenceManager` — responsabilidades: crear base, guardar/cargar tickets y notas, manejar archivos por ticket.
- `FileUtils` — utilidades estáticas de lectura/escritura.

### Paquete `reports`
- `ReportManager` — genera listados en consola y exporta CSV/TXT (pending, completed, Top-K).

### Paquete `util`
- `StateMachine` — valida transiciones de `TicketState`.
- `SystemClock` — abstracción de tiempo para timestamps y formateo.

### Paquete `controller.command`
- `AddNoteCommand` — comando para añadir nota y soportar undo.
- `AddTicketCommand` — comando para encolar ticket y soportar undo.
- `CloseCaseCommand` — comando para cerrar ticket (mover a completados) y soportar undo.

### Paquete `controller`
- `ActionStack` — gestiona pilas de undo/redo (almacena `IAction`).
- `IAction` — interfaz mínima (execute/undo).
- `CaeController` — controlador principal que orquesta colas, comandos, persistencia y reportes.
- `CLIHelper` — responsable de interacción en consola (menús, validaciones, mensajes).

---

## Persistencia y formatos sugeridos
- Base path por defecto: `data/`
- Sugerencia de archivos:
    - `data/pending_normal.csv`
    - `data/pending_urgent.csv`
    - `data/attended_history.csv`
    - `data/notes_ticket_<id>.csv`
- Recomendación: CSV con encabezado `id,student,procedureType,state,timestamp` y escape de comas en campos.

---

## Pruebas unitarias (clases a testear)
Se realizaron tests unitarios (JUnit 5) sobre las siguientes clases:

**Estructuras**
- `SimpleList<T>`
- `Queue<T>`
- `Stack<T>`

**Controlador**
- `AtentionQueue`

**Command Pattern**
- `AddNoteCommand`
- `AddTicketCommand`
- `CloseCaseCommand`

**Utilidades**
- `StateMachine`

---

## Evidencias / Capturas

- Diagrama de clases (PlantUML renderizado):  
  ![Diagrama de clases](https://www.plantuml.com/plantuml/svg/h5XVR-Eu4N_Ffo3oq9Dxsktj0Nr9KQ1UhRDhDB5JHsdHfu0HnpORYbJ9KPnSST-zeaZJeYpPcyCzsPpvpVyXETJV35ADTIakVY1tWafykBqXXQ364CRfHjEI98KgAuhyIG3PSTmUS9y-_lpJpt_--EcdIIET2Q0I696q14DG4VgYE2DKRWHyUDAqU0OqpP8HBWHig22ceWMOlv0DID2yC0HaeHWmPyNKwpL_1KEIntnoST5Wd-a6o2LJ9UNoalplWdX7SsvrjmH2Ff36w1NX4ltIOCr0OaExHytbnfChhGfWjORyhOA6UTTT1r6AbkSCtDkLvqY5y8qRLFhDAkPb9U2cMJywg4DzQ7z-zE99tv7qj0yJBxq1dBDa4cAoDDFIze46fFVHWYEVWv1Z7uVLDROuX9PCXeCBrbn43XQIwtXHB2qYedWWPSpQIzIJ0Vr2aIiPT5cPXccXlrw4ObjAAFMGc6SXBy4WBQk6SQCAAhvGXBmXnhKPjtuEa8wQw6PpUP0SJ5E-rs6viHOFGkLa8d6N4P1rUQXYoCjiyPWjRwRj_uVLrzaYd-tfqtosoERBHRlEbhTtDxDy-cNPBkzcYozp2txyiipk8njHwRoLRBRAvzVpR2z-EyrNy-p1s_ssl5tUBBzEWx7LB9-lbe_J-_dNnJH_M3dOCbyjMqk_TdQuGLqNs5WptLs-K0mUyr3w5ofgsyJBf_z0WPueuHKRceE60jf54kCdKLCTztWmimNAOesXTQhQRA-rafX8s3L5es5BJJg8pqrKeu0rbonvXhS38_pp20sbUe7J6CD_WTu1mSsihD1ju2UbxD7iEOK0gfDU_F-eeOP-w6iRMozsee7gaNo0_AzL4p8TXyh0SO-yhm2UO-hf0E9eVvSqnhs0JSKUvzF2xjcG35JLYJOuxYx7KcvREseSRw2-FzxX6auHGLgVss86nwNI9HMETXKLsWDglG699m3KAcR0llk-eumvSe97RM1tgACSrtnBpODrnWFCG39i5NFbFHbGlm6SHkwUlSDoXLJyYtBaSXEdsLs68NkM6QSde9pNewZuTAj06suGP07Tu-tkGBwbia7gKC8dQk2EuZQS_-Ilv99Hf7-wZ1Dlw0liSuVkToZuIGU-pzBM_Pw3szlGN2izMb-XABD7NLA1P8shGtsL4czjYDdhwciku06vCFjikAcC5wG4t2fc3j5heCp2awgRg_ZIzR0RBi4SuspG7hNJ7E5OMqeA9R4xnHttAArixBdwRTBhHchsoWq6_uQGSKjfg9J6wBPSjQIeasm9jsftvmhp-StUvQVQ92NmQfLavkLWkmd1lDn5ZdLqPyhgG62_izvSLN___DOstsdDpsu3lyD4dCaQkUYciHshRccnvH925hdv9nMSvPfAmzl9RgrL6G_KAK4LKmOQHGYr0xQ0LuSm0tfwRSd059e_mVjjHwDZ2E_D89IPKCLpY4wgtS0i6dZNzaH4M-BA_cWvljFMBJHXsDUMafEoSKdibj5A2D0V2rMML39N82uHz9eMGC9TxUt2AnIrVGRvTIsPEhejtSCZSnh3KUkQAoIlyzhpZn6lCXPFUc9fLsBoFg-SXa6_JbtI4dPktRzK-ogJSLNfW9RtU9y9PI2ZvZkpMcbuuQeszwElRADgNS29g8lzUUEePXJwOt1kl6RZrjBpXiwdQholkmUEQ-Ls1WtfimeSvIgUJiDCVf9_k1ytt21enqjeuP-p7Xox6sJ2ujICkgLcPP4H9qn7U_SjEtRGWqmND1fjPyPRmNcL7T1eWleDJ24BLGdff_5WVTMRjFUeG-4ivB3o_EfulhiQ6Feysjte0HXVvnvZEdUJ2omiFQ8GoiLyEEO3XpVqx6R-3KG5EcgKE0t9cV3JySZJxmqwFHrlUYhKT2ZAj1zW62QGQap2QbiZKpiP28K6YZ3wlGu7FjEb_W5rwFp-kyOrK7lD9qUyDPTKy5_sDd7ez0sVoN8r34l9wUzdRZBd1ltib_IcYc9BvGOwvwytC7SV6Dt5C71CdpqotAVT1kdg6O-Uxve2xHFVMVxyDcUnVwDF-L3-C1fKke7FvLebfTb4HW9tfhNIu-pxkYZ0c770L836GJOrpqcbLLdXqDmJCClAbh6FJ4d9vRuS9NqDaTkVW_2_mIpKiPMGuQwdtu0oq0bo570y4_z0LY3QZZ9RNXdo1zAEsoL8T5II5EwJ_-J23msNdox97viNskN_ApDZ7OHX68ZkznMeOwKQPWyLVyJ0oiTJfuJSjISafWv-BeXOqVDnYvYdFQfDpiePN9QrFJLRJ0t5JOC4dhbV3O3i0lxBjFWmMw872Rmi2Ss1F0Ez2nlrA6fxoaSavp1_T54uywFBv9TvZ6q0ap14hETUU52bIlTB_nwCvRs0ysi1liaB4WdaxQHgDKyrbn-3Uuw1x0w3WzP7l5QggGbhbVpGLC5UzXt6UUh09SdIhiF807MAVcXwbXuIYV24-KazgFxW6H48YuX8M8AK0lTj_EgFnJS0)

---

## Decisiones de diseño y casos borde
- **Máquina de estados**: usar `StateMachine` para validar todas las transiciones de `TicketState`.
- **Persistencia**: `PersistenceManager` debe recuperarse de archivos faltantes y escribir en UTF-8.
- **Undo/Redo**: `ActionStack` solo registra acciones una vez que su `execute()` fue invocado.
- **Casos borde críticos**:
    - `Stack.pop()` en vacío → lanzar excepción controlada.
    - `Queue.dequeue()` en vacío → lanzar excepción controlada.
    - `StateMachine` rechaza transiciones inválidas (p. ej. `COMPLETADO` → `EN_ATENCION`).
    - `PersistenceManager` maneja archivos corruptos o faltantes sin fallar la aplicación.

---

## Contacto
**Docente / Coordinador:** Andrés R. Navas Castellanos
