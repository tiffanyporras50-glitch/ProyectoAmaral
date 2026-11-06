/* =====================================================================
   Distribuidora Amaral - Grupo 5 - SC-403
   Script de creación y datos de prueba de la base de datos
   Ejecutar completo en MySQL Workbench (rayo ⚡ sin cursor en una línea)
   ===================================================================== */

-- 1. Base de datos y usuario propio del proyecto (NO es el de la Tienda)
DROP SCHEMA IF EXISTS amaral;
DROP USER IF EXISTS 'usuario_amaral'@'%';

CREATE SCHEMA amaral;
CREATE USER 'usuario_amaral'@'%' IDENTIFIED BY 'Amaral_Clave.';
GRANT ALL PRIVILEGES ON amaral.* TO 'usuario_amaral'@'%';
FLUSH PRIVILEGES;

USE amaral;

/* =====================================================================
   2. TABLAS
   ===================================================================== */

-- Roles del sistema: ADMIN, CLIENTE, VENDEDOR, BODEGUERO, CHOFER
CREATE TABLE rol (
  id_rol INT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(30) NOT NULL UNIQUE,
  PRIMARY KEY (id_rol)
) ENGINE = InnoDB;

-- Personas que usan el sistema (clientes = comercios/supermercados)
CREATE TABLE usuario (
  id_usuario INT NOT NULL AUTO_INCREMENT,
  username VARCHAR(30) NOT NULL UNIQUE,
  password VARCHAR(512) NOT NULL,
  nombre VARCHAR(50) NOT NULL,
  apellidos VARCHAR(80),
  nombre_comercio VARCHAR(100),          -- solo para clientes
  correo VARCHAR(75),
  telefono VARCHAR(20),
  direccion VARCHAR(255),                -- dirección de entrega del cliente
  ruta_imagen VARCHAR(1024),
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_usuario)
) ENGINE = InnoDB;

-- Relación usuario - rol
CREATE TABLE usuario_rol (
  id_usuario INT NOT NULL,
  id_rol INT NOT NULL,
  PRIMARY KEY (id_usuario, id_rol),
  FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
  FOREIGN KEY (id_rol) REFERENCES rol(id_rol)
) ENGINE = InnoDB;

-- Líneas de producto de la distribuidora
CREATE TABLE categoria (
  id_categoria INT NOT NULL AUTO_INCREMENT,
  descripcion VARCHAR(50) NOT NULL UNIQUE,
  ruta_imagen VARCHAR(1024),
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id_categoria)
) ENGINE = InnoDB;

-- Productos del catálogo
CREATE TABLE producto (
  id_producto INT NOT NULL AUTO_INCREMENT,
  id_categoria INT NOT NULL,
  nombre VARCHAR(100) NOT NULL,
  marca VARCHAR(50),
  presentacion VARCHAR(50),              -- ej: "Caja 24 unidades", "Botella 1 L"
  precio DECIMAL(12,2) NOT NULL CHECK (precio >= 0),
  existencias INT NOT NULL DEFAULT 0 CHECK (existencias >= 0),
  ruta_imagen VARCHAR(1024),
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id_producto),
  FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria)
) ENGINE = InnoDB;

-- Entradas y salidas de bodega (HU 10)
CREATE TABLE movimiento_inventario (
  id_movimiento INT NOT NULL AUTO_INCREMENT,
  id_producto INT NOT NULL,
  id_usuario INT NOT NULL,               -- bodeguero que registra
  tipo VARCHAR(10) NOT NULL CHECK (tipo IN ('ENTRADA','SALIDA')),
  cantidad INT NOT NULL CHECK (cantidad > 0),
  observacion VARCHAR(255),
  fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_movimiento),
  FOREIGN KEY (id_producto) REFERENCES producto(id_producto),
  FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
) ENGINE = InnoDB;

-- TABLA TRANSACCIONAL PRINCIPAL: pedidos (HU 13 a 18)
-- estado: RECIBIDO, CONFIRMADO, LISTO_DESPACHO, ENTREGADO, NO_ENTREGADO, CANCELADO
CREATE TABLE pedido (
  id_pedido INT NOT NULL AUTO_INCREMENT,
  id_cliente INT NOT NULL,
  id_vendedor INT,                       -- solo si lo registró un vendedor (HU 16)
  fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  estado VARCHAR(20) NOT NULL DEFAULT 'RECIBIDO',
  metodo_pago VARCHAR(20) NOT NULL,      -- EFECTIVO, TRANSFERENCIA, SINPE, CREDITO
  forma_entrega VARCHAR(20) NOT NULL,    -- ENVIO, RETIRO
  subtotal DECIMAL(12,2) NOT NULL DEFAULT 0,
  iva DECIMAL(12,2) NOT NULL DEFAULT 0,
  total DECIMAL(12,2) NOT NULL DEFAULT 0,
  observacion VARCHAR(255),
  PRIMARY KEY (id_pedido),
  FOREIGN KEY (id_cliente) REFERENCES usuario(id_usuario),
  FOREIGN KEY (id_vendedor) REFERENCES usuario(id_usuario)
) ENGINE = InnoDB;

-- Productos de cada pedido
CREATE TABLE detalle_pedido (
  id_detalle INT NOT NULL AUTO_INCREMENT,
  id_pedido INT NOT NULL,
  id_producto INT NOT NULL,
  cantidad INT NOT NULL CHECK (cantidad > 0),
  precio_unitario DECIMAL(12,2) NOT NULL,
  PRIMARY KEY (id_detalle),
  FOREIGN KEY (id_pedido) REFERENCES pedido(id_pedido),
  FOREIGN KEY (id_producto) REFERENCES producto(id_producto)
) ENGINE = InnoDB;

-- Asignación de pedidos a ruta y resultado de la entrega (HU 19 a 21)
CREATE TABLE entrega (
  id_entrega INT NOT NULL AUTO_INCREMENT,
  id_pedido INT NOT NULL UNIQUE,
  id_chofer INT NOT NULL,
  fecha_entrega DATE NOT NULL,
  resultado VARCHAR(20),                 -- NULL = pendiente, ENTREGADO, NO_ENTREGADO
  motivo VARCHAR(255),                   -- obligatorio si NO_ENTREGADO
  fecha_registro TIMESTAMP NULL,
  PRIMARY KEY (id_entrega),
  FOREIGN KEY (id_pedido) REFERENCES pedido(id_pedido),
  FOREIGN KEY (id_chofer) REFERENCES usuario(id_usuario)
) ENGINE = InnoDB;

/* =====================================================================
   3. DATOS DE PRUEBA (ficticios)
   Las contraseñas están en texto plano temporalmente; cuando veamos
   Spring Security en clase se cambian por la versión encriptada.
   ===================================================================== */

INSERT INTO rol (nombre) VALUES
 ('ADMIN'), ('CLIENTE'), ('VENDEDOR'), ('BODEGUERO'), ('CHOFER');

INSERT INTO usuario (username, password, nombre, apellidos, nombre_comercio, correo, telefono, direccion) VALUES
 ('admin',      'Amaral123', 'Laura',   'Mora Quesada',    NULL,                     'admin@amaral.test',      '2710-0001', 'Oficinas centrales, Guácimo, Limón'),
 ('superpinar', 'Amaral123', 'Carlos',  'Rodríguez Solís', 'Supermercado El Pinar',  'pinar@cliente.test',     '2716-1122', '200 m norte del parque, Guácimo, Limón'),
 ('minisuper',  'Amaral123', 'Ana',     'Vargas Brenes',   'Minisúper La Esquina',   'esquina@cliente.test',   '2754-3344', 'Frente a la escuela, Sixaola, Limón'),
 ('vendedor1',  'Amaral123', 'Diego',   'Castro Arias',    NULL,                     'diego@amaral.test',      '8800-1001', NULL),
 ('bodega1',    'Amaral123', 'Marco',   'Jiménez Rojas',   NULL,                     'bodega@amaral.test',     '8800-1002', NULL),
 ('chofer1',    'Amaral123', 'Andrés',  'Picado León',     NULL,                     'chofer@amaral.test',     '8800-1003', NULL);

INSERT INTO usuario_rol (id_usuario, id_rol) VALUES
 (1,1), (2,2), (3,2), (4,3), (5,4), (6,5);

INSERT INTO categoria (descripcion) VALUES
 ('Cuidado personal'), ('Ferretería'), ('Ciclo'), ('Medicamentos de venta libre'),
 ('Librería y escolares'), ('Lubricentro y automotriz'), ('Bazar'),
 ('Hogar y cocina'), ('Limpieza'), ('Papelería'), ('Bebés'), ('Deportes'), ('Juguetería');

INSERT INTO producto (id_categoria, nombre, marca, presentacion, precio, existencias) VALUES
 (1,  'Crema dental 100 ml',            'Colgate',   'Caja 12 unidades',   9600.00, 40),
 (1,  'Champú 400 ml',                  'Sedal',     'Caja 6 unidades',   10200.00, 25),
 (1,  'Jabón de tocador 3 pack',        'Protex',    'Paca 12 unidades',  14500.00, 30),
 (2,  'Cinta aislante 18 m',            'Truper',    'Caja 10 unidades',   6500.00, 50),
 (2,  'Tornillos para madera 1"',       'Fiero',     'Bolsa 100 unidades', 2800.00, 60),
 (3,  'Neumático bicicleta aro 26',     'Kenda',     'Unidad',             5900.00, 12),
 (4,  'Acetaminofén 500 mg',            'Panadol',   'Caja 100 sobres',   18000.00, 20),
 (5,  'Cuaderno cosido 100 hojas',      'Scribe',    'Paca 10 unidades',  11000.00, 35),
 (5,  'Lápiz de grafito #2',            'Mongol',    'Caja 12 unidades',   2400.00, 80),
 (6,  'Aceite de motor 20W-50 1 L',     'Castrol',   'Caja 12 unidades',  42000.00, 15),
 (7,  'Vasos desechables 7 oz',         'Darnel',    'Paca 50 unidades',   1500.00, 100),
 (8,  'Sartén antiadherente 24 cm',     'Imusa',     'Unidad',             8900.00, 10),
 (9,  'Cloro 1 galón',                  'Clorox',    'Caja 4 unidades',   11600.00, 43),
 (9,  'Detergente en polvo 1 kg',       'Irex',      'Paca 10 unidades',  19500.00, 30),
 (10, 'Papel bond carta 500 hojas',     'Chamex',    'Resma',              4200.00, 40),
 (11, 'Pañales etapa 3',                'Huggies',   'Paquete 48 unidades',9800.00, 18),
 (11, 'Toallitas húmedas',              'Pequeñín',  'Caja 12 paquetes',  15600.00, 0),
 (12, 'Bola de fútbol #5',              'Molten',    'Unidad',            12500.00, 8),
 (13, 'Carrito de juguete',             'Genérico',  'Caja 12 unidades',  18000.00, 6),
 (13, 'Rompecabezas 100 piezas',        'Genérico',  'Unidad',             3500.00, 0);

INSERT INTO movimiento_inventario (id_producto, id_usuario, tipo, cantidad, observacion) VALUES
 (1, 5, 'ENTRADA', 40, 'Ingreso inicial de inventario'),
 (13,5, 'ENTRADA', 45, 'Ingreso inicial de inventario'),
 (13,5, 'SALIDA',   2, 'Producto dañado en bodega');

-- Pedido 1: hecho por el cliente en línea, ya confirmado
INSERT INTO pedido (id_cliente, id_vendedor, estado, metodo_pago, forma_entrega, subtotal, iva, total) VALUES
 (2, NULL, 'CONFIRMADO', 'TRANSFERENCIA', 'ENVIO', 42400.00, 5512.00, 47912.00);
INSERT INTO detalle_pedido (id_pedido, id_producto, cantidad, precio_unitario) VALUES
 (1, 1, 2, 9600.00),
 (1, 13, 2, 11600.00);

-- Pedido 2: registrado por el vendedor para un cliente, listo para despacho y asignado a ruta
INSERT INTO pedido (id_cliente, id_vendedor, estado, metodo_pago, forma_entrega, subtotal, iva, total) VALUES
 (3, 4, 'LISTO_DESPACHO', 'EFECTIVO', 'ENVIO', 22000.00, 2860.00, 24860.00);
INSERT INTO detalle_pedido (id_pedido, id_producto, cantidad, precio_unitario) VALUES
 (2, 8, 2, 11000.00);
INSERT INTO entrega (id_pedido, id_chofer, fecha_entrega) VALUES
 (2, 6, CURDATE());

-- Pedido 3: recién recibido
INSERT INTO pedido (id_cliente, id_vendedor, estado, metodo_pago, forma_entrega, subtotal, iva, total) VALUES
 (2, NULL, 'RECIBIDO', 'SINPE', 'RETIRO', 19500.00, 2535.00, 22035.00);
INSERT INTO detalle_pedido (id_pedido, id_producto, cantidad, precio_unitario) VALUES
 (3, 14, 1, 19500.00);
