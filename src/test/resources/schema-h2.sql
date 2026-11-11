-- Esquema para las pruebas automáticas (H2 en modo MySQL).
-- Copia de la sección "2. TABLAS" de docs/amaral.sql sin "ENGINE = InnoDB".
-- Las pruebas usan las mismas tablas que MySQL y Hibernate solo las valida
-- (ddl-auto=validate), así se detecta cualquier entidad que no coincida.
-- Si cambian las tablas en docs/amaral.sql, actualizar también este archivo.

DROP ALL OBJECTS;

-- Roles del sistema: ADMIN, CLIENTE, VENDEDOR, BODEGUERO, CHOFER
CREATE TABLE rol (
  id_rol INT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(30) NOT NULL UNIQUE,
  PRIMARY KEY (id_rol)
);

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
);

-- Relación usuario - rol
CREATE TABLE usuario_rol (
  id_usuario INT NOT NULL,
  id_rol INT NOT NULL,
  PRIMARY KEY (id_usuario, id_rol),
  FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
  FOREIGN KEY (id_rol) REFERENCES rol(id_rol)
);

-- Líneas de producto de la distribuidora
CREATE TABLE categoria (
  id_categoria INT NOT NULL AUTO_INCREMENT,
  descripcion VARCHAR(50) NOT NULL UNIQUE,
  ruta_imagen VARCHAR(1024),
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id_categoria)
);

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
);

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
);

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
);

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
);

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
);
