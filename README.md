# Distribuidora Amaral – Sistema web de pedidos

Proyecto final del curso **SC-403 Desarrollo de Aplicaciones Web y Patrones** – Universidad Fidélitas.
**Grupo 5:** José Alejandro Sánchez Solano · María Paula Calvo Gamboa · Jezrael Hernández Jiménez · Tiffany Porras Marín

## Descripción
Distribuidora Amaral es una empresa familiar costarricense que tiene más de 30 años de trayectoria. Distribuye productos a supermercados y comercios de todo el país.
Esta aplicación web permite:
- que los clientes consulten el catálogo y hagan pedidos en línea;
- que los vendedores registren pedidos a nombre de sus clientes;
- que el administrador gestione los usuarios, los productos y los pedidos;
- que bodega prepare los pedidos;
- que los choferes confirmen las entregas de su ruta.

## Tecnologías
- Java 21 + Spring Boot 4.0.6 (Maven, NetBeans)
- Thymeleaf (plantillas, fragmentos e internacionalización)
- Bootstrap 5.3 y Font Awesome por WebJars
- Spring Data JPA / Hibernate + MySQL 8
- Firebase Storage para las imágenes de los productos

## Estructura del proyecto (MVC)
```
src/main/java/DistribuidoraAmaral/
 ├── domain/        Entidades JPA
 ├── repository/    Interfaces JpaRepository
 ├── service/       Lógica de negocio (@Service)
 ├── controller/    Controladores (@Controller)
src/main/resources/
 ├── templates/     Vistas Thymeleaf (general/plantilla.html + carpeta por módulo)
 ├── static/        CSS, JS
 ├── messages*.properties   Archivos de idioma
docs/amaral.sql     Script de la base de datos
```

## Cómo ejecutar
1. Abrir **MySQL Workbench** con el usuario root y ejecutar completo el archivo `docs/amaral.sql`.
   El script crea la base `amaral`, el usuario `usuario_amaral` y los datos de prueba.
2. Abrir el proyecto en **NetBeans**: *File → Open Project* y escoger la carpeta del repositorio.
3. Si el proyecto usa imágenes, colocar el archivo de credenciales de Firebase en `src/main/resources/` (no se sube a GitHub).
4. Ejecutar el proyecto con *Run* y abrir `http://localhost`.

## Base de datos (9 tablas)
| Tabla | Uso |
|---|---|
| rol, usuario, usuario_rol | Usuarios y roles: ADMIN, CLIENTE, VENDEDOR, BODEGUERO, CHOFER |
| categoria, producto | Catálogo |
| movimiento_inventario | Entradas y salidas de bodega |
| **pedido**, detalle_pedido | **Transacciones**: los pedidos y sus productos |
| entrega | Asignación a la ruta y resultado de la entrega |

## Usuarios de prueba
| Usuario | Rol | Contraseña |
|---|---|---|
| admin | Administrador | Amaral123 |
| superpinar | Cliente | Amaral123 |
| minisuper | Cliente | Amaral123 |
| vendedor1 | Vendedor | Amaral123 |
| bodega1 | Bodeguero | Amaral123 |
| chofer1 | Chofer | Amaral123 |

## Forma de trabajo en GitHub
- `main` es la versión estable. Nadie trabaja directo en `main`.
- Cada funcionalidad se hace en su propia rama, por ejemplo `feature/estructura-base` o `feature/catalogo`.
- Al terminar, se abre un **Pull Request** hacia `main` y **otro integrante lo revisa** antes de aceptarlo.
- Antes de empezar a trabajar, hacer `git pull` en `main`.

## Estado del avance
| Avance | Estado |
|---|---|
| 1 – Historias de usuario y prototipo | ✅ Entregado |
| 2 – Implementación del 50% | 🚧 En progreso: estructura base y script de base de datos listos |

### Módulos implementados en el Avance 2
| Módulo | Ruta | Estado |
|---|---|---|
| Plantilla, inicio e idiomas | `/` | ✅ |
| Catálogo de productos (HU 11–12) | `/catalogo` | ✅ |
| CRUD de productos (HU 06–09) | `/producto/listado` | ✅ |
| Gestionar pedidos (HU 17) | `/pedido/listado` | ✅ |
| CRUD de categorías | `/categoria/listado` | ✅ |

**HU 17 – Gestionar pedidos:** el administrador ve los pedidos (número, cliente, fecha, total y estado),
los filtra por estado, consulta el detalle con sus productos y cambia el estado. Solo se permiten estas transiciones:

| Estado actual | Puede pasar a |
|---|---|
| RECIBIDO | CONFIRMADO, CANCELADO |
| CONFIRMADO | LISTO_DESPACHO, CANCELADO |
| LISTO_DESPACHO | ENTREGADO, NO_ENTREGADO |
| NO_ENTREGADO | LISTO_DESPACHO (reintento), CANCELADO |
| ENTREGADO, CANCELADO | — (estados finales) |

Para CANCELADO y NO_ENTREGADO se pide un motivo, que se agrega a la observación del pedido.

**CRUD de categorías:** listar, registrar, consultar (con sus productos), editar y eliminar.
No se permiten nombres duplicados ni vacíos. Si la categoría tiene productos no se borra: se desactiva
y deja de aparecer en los filtros del catálogo (se puede reactivar al editarla).

## Pruebas automáticas
```
mvn test
```
Las pruebas usan una base **H2 en memoria** (`src/test/resources/application.properties`), por lo que
no necesitan MySQL ni modifican la base `amaral`. En NetBeans: clic derecho al proyecto → *Test*.
