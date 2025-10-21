🚀 Taller Grupal: Sistema de Gestión de Casos (CAE)
Este proyecto implementa un sistema de gestión de tickets de atención (casos) utilizando estructuras de datos personalizadas (Cola, Pila y Lista Enlazada) en Java.
Pruebas de que trabajamos todos: [Imagen en Canva](https://www.canva.com/design/DAG2S5DK-vU/I3c3AwEQUK5G8kqS6vg3Ww/edit)

📐 Decisiones de Diseño
El proyecto se dividió en tres componentes principales para una clara separación de responsabilidades:

📦 Estructuras: Contiene las estructuras de datos genéricas (Node, Queue, Stack, SimpleList) que son la base del sistema.

🏛️ Dominio: Contiene las clases que modelan el problema de negocio (Ticket, Nota, EstadoTicket).

🖥️ Main: Controla el flujo del programa, maneja la entrada/salida (E/S) del usuario y coordina la lógica.

🚦 Catálogo de Estados
El estado de un ticket es gestionado por el enum EstadoTicket.java:

EN_COLA: Estado inicial del ticket al ser creado (Opción 1).

EN_ATENCION: El ticket se extrae de la cola (ticketQueue.dequeue()) al ser atendido (Opción 2).

COMPLETADO: El operador finaliza la atención y el ticket se mueve al historial (finishedCases).

🛡️ Casos Borde Manejados
El sistema fue diseñado para ser robusto y manejar las siguientes situaciones:

Atender sin casos: Verifica si ticketQueue está vacía antes de un dequeue.

Ver cola vacía: Informa al usuario si no hay casos en espera.

Undo/Redo en vacío: Verifica si undoStack o redoStack están vacíos antes de un pop.

Búsqueda global: La consulta de historial busca tanto en la cola de espera como en la de finalizados.

Eliminación Segura: El método remove de SimpleList maneja correctamente el caso de eliminar el nodo head.

Estructuras vacías: Todas las estructuras lanzan excepciones (EmptyStackException, NoSuchElementException) en operaciones inválidas (ej. pop en una pila vacía).

📖 Manual de Usuario
Guía rápida para operar el sistema de gestión de casos.

1. Recibir Nuevo Caso
Registra un nuevo trámite en el sistema.

Seleccione la opción 1 en el menú principal.

Ingrese el Nombre del estudiante.

Ingrese el Tipo de trámite (ej. "Retiro de materia", "Homologación").

Resultado: El sistema confirmará la creación con un ID único y pondrá el caso al final de la cola.

>> Caso Nro. [ID] recibido y puesto en cola correctamente <<

2. Atender Siguiente Caso
Toma el primer caso de la cola (el más antiguo) para gestionarlo.

Seleccione la opción 2 en el menú principal.

El sistema asignará el caso: Atendiendo caso ID: [ID] a nombre de: [Nombre del Estudiante].

Nota: Si no hay casos, el sistema le informará y volverá al menú.

Al atender, se accede a un submenú de atención:

1. Agregar nota
2. Deshacer última nota
3. Rehacer nota
4. Finalizar caso
   
Seleccione una opción:
1. Agregar nota: Añade una observación al historial del caso (ej. "Se verifica sílabo").

2. Deshacer última nota: Elimina la última nota agregada. Se puede usar múltiples veces.

3. Rehacer nota: Restaura una nota eliminada con "Deshacer".

Importante: Si agrega una nota nueva, se pierde el historial de "rehacer".

4. Finalizar caso: Mueve el caso al historial de completados y regresa al menú principal.

3. Ver Casos en Espera
Muestra un resumen de todos los casos pendientes en la cola.

Seleccione la opción 3 en el menú principal.

Resultado: Se enlista un conteo y el detalle de los casos:

Casos en espera: [Número]
ID: [1] --- Nombre: [Ana] --- Tipo de trámite: [Justificación]
ID: [2] --- Nombre: [Luis] --- Tipo de trámite: [Retiro]

4. Consultar Historial de Caso (por ID)
Permite buscar y ver los detalles completos de cualquier caso (en cola, en atención o completado).

Seleccione la opción 4 en el menú principal.

Ingrese el ID del caso a buscar.

Resultado: Muestra el informe completo del ticket:

Historial del caso [ID]
Estudiante: [Nombre del Estudiante]
Trámite: [Tipo de Trámite]
Estado: [EN_COLA / EN_ATENCION / COMPLETADO]
Notas:
- [2025-10-19 10:30:01] Se recepta el caso
- [2025-10-19 10:35:15] Se deriva al coordinador
Nota: Si el ID no existe, el sistema le informará.

0. Salir
Cierra la aplicación de forma segura.

Seleccione la opción 0 en el menú principal.

El sistema mostrará Saliendo del sistema... y terminará.
