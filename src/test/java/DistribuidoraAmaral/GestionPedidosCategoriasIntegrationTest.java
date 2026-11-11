package DistribuidoraAmaral;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import DistribuidoraAmaral.domain.Categoria;
import DistribuidoraAmaral.domain.Cliente;
import DistribuidoraAmaral.domain.DetallePedido;
import DistribuidoraAmaral.domain.EstadoPedido;
import DistribuidoraAmaral.domain.Pedido;
import DistribuidoraAmaral.domain.Producto;
import DistribuidoraAmaral.repository.CategoriaRepository;
import DistribuidoraAmaral.repository.PedidoRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * Pruebas de integración de HU 17 (pedidos) y del CRUD de categorías:
 * controlador + servicio + repositorio + vistas Thymeleaf, con base H2 en memoria.
 * Cada prueba se revierte al terminar (@Transactional).
 *
 * @author Jezrael Hernández
 */
@SpringBootTest
@Transactional
class GestionPedidosCategoriasIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private EntityManager em;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    private MockMvc mvc;
    private Pedido pedidoRecibido;
    private Categoria limpieza;
    private Categoria vacia;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();

        // El cliente se inserta con SQL (como en docs/amaral.sql) porque la entidad
        // Cliente es de solo lectura y no maneja columnas como password.
        // Así la prueba no depende de la entidad Usuario del CRUD de usuarios.
        em.createNativeQuery("INSERT INTO usuario (id_usuario, username, password, nombre, apellidos,"
                + " nombre_comercio, telefono, direccion) VALUES (2, 'superpinar', 'Amaral123', 'Carlos',"
                + " 'Rodríguez Solís', 'Supermercado El Pinar', '2716-1122',"
                + " '200 m norte del parque, Guácimo, Limón')").executeUpdate();
        Cliente cliente = em.find(Cliente.class, 2);

        limpieza = new Categoria();
        limpieza.setDescripcion("Limpieza");
        em.persist(limpieza);

        vacia = new Categoria();
        vacia.setDescripcion("Temporal");
        em.persist(vacia);

        Producto cloro = new Producto();
        cloro.setCategoria(limpieza);
        cloro.setNombre("Cloro 1 galón");
        cloro.setMarca("Clorox");
        cloro.setPresentacion("Caja 4 unidades");
        cloro.setPrecio(new BigDecimal("11600.00"));
        cloro.setExistencias(43);
        em.persist(cloro);

        pedidoRecibido = new Pedido();
        pedidoRecibido.setCliente(cliente);
        pedidoRecibido.setEstado(EstadoPedido.RECIBIDO);
        pedidoRecibido.setMetodoPago("SINPE");
        pedidoRecibido.setFormaEntrega("RETIRO");
        pedidoRecibido.setSubtotal(new BigDecimal("23200.00"));
        pedidoRecibido.setIva(new BigDecimal("3016.00"));
        pedidoRecibido.setTotal(new BigDecimal("26216.00"));
        em.persist(pedidoRecibido);

        DetallePedido detalle = new DetallePedido();
        detalle.setPedido(pedidoRecibido);
        detalle.setProducto(cloro);
        detalle.setCantidad(2);
        detalle.setPrecioUnitario(new BigDecimal("11600.00"));
        em.persist(detalle);

        em.flush();
        em.clear();
    }

    private Pedido recargarPedido() {
        em.flush();
        em.clear();
        return pedidoRepository.findById(pedidoRecibido.getIdPedido()).orElseThrow();
    }

    // ---------------- HU 17: pedidos ----------------

    @Test
    void listadoMuestraLosPedidosConSuInformacion() throws Exception {
        mvc.perform(get("/pedido/listado"))
                .andExpect(status().isOk())
                .andExpect(view().name("pedido/listado"))
                .andExpect(model().attribute("pedidos", hasSize(1)))
                .andExpect(content().string(containsString("Supermercado El Pinar")))
                .andExpect(content().string(containsString("26,216.00")));
    }

    @Test
    void listadoFiltraPorEstado() throws Exception {
        mvc.perform(get("/pedido/listado").param("estado", "RECIBIDO"))
                .andExpect(model().attribute("pedidos", hasSize(1)));
        mvc.perform(get("/pedido/listado").param("estado", "ENTREGADO"))
                .andExpect(model().attribute("pedidos", hasSize(0)));
        // Un estado inválido muestra todos los pedidos
        mvc.perform(get("/pedido/listado").param("estado", "XYZ"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("pedidos", hasSize(1)));
    }

    @Test
    void detalleMuestraProductosYTotales() throws Exception {
        mvc.perform(get("/pedido/detalle/" + pedidoRecibido.getIdPedido()))
                .andExpect(status().isOk())
                .andExpect(view().name("pedido/detalle"))
                .andExpect(content().string(containsString("Cloro 1 galón")))
                .andExpect(content().string(containsString("23,200.00")))
                .andExpect(content().string(containsString("value=\"CONFIRMADO\"")));
    }

    @Test
    void detalleDePedidoInexistenteRegresaAlListado() throws Exception {
        mvc.perform(get("/pedido/detalle/9999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/pedido/listado"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void confirmarPedidoSeGuardaEnLaBaseDeDatos() throws Exception {
        mvc.perform(post("/pedido/estado")
                .param("idPedido", pedidoRecibido.getIdPedido().toString())
                .param("estado", "CONFIRMADO"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("todoOk"));

        assertEquals(EstadoPedido.CONFIRMADO, recargarPedido().getEstado());
    }

    @Test
    void flujoCompletoHastaEntregado() throws Exception {
        for (String estado : new String[]{"CONFIRMADO", "LISTO_DESPACHO", "ENTREGADO"}) {
            mvc.perform(post("/pedido/estado")
                    .param("idPedido", pedidoRecibido.getIdPedido().toString())
                    .param("estado", estado))
                    .andExpect(flash().attributeExists("todoOk"));
        }
        assertEquals(EstadoPedido.ENTREGADO, recargarPedido().getEstado());
    }

    @Test
    void transicionIncorrectaNoCambiaElEstado() throws Exception {
        mvc.perform(post("/pedido/estado")
                .param("idPedido", pedidoRecibido.getIdPedido().toString())
                .param("estado", "ENTREGADO"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("error"));

        assertEquals(EstadoPedido.RECIBIDO, recargarPedido().getEstado());
    }

    @Test
    void cancelarRequiereMotivoYLoGuarda() throws Exception {
        mvc.perform(post("/pedido/estado")
                .param("idPedido", pedidoRecibido.getIdPedido().toString())
                .param("estado", "CANCELADO"))
                .andExpect(flash().attributeExists("error"));
        assertEquals(EstadoPedido.RECIBIDO, recargarPedido().getEstado());

        mvc.perform(post("/pedido/estado")
                .param("idPedido", pedidoRecibido.getIdPedido().toString())
                .param("estado", "CANCELADO")
                .param("motivo", "Cliente pidió cancelar"))
                .andExpect(flash().attributeExists("todoOk"));

        Pedido cancelado = recargarPedido();
        assertEquals(EstadoPedido.CANCELADO, cancelado.getEstado());
        assertEquals("[CANCELADO] Cliente pidió cancelar", cancelado.getObservacion());
    }

    // ---------------- CRUD de categorías ----------------

    @Test
    void listadoDeCategorias() throws Exception {
        mvc.perform(get("/categoria/listado"))
                .andExpect(status().isOk())
                .andExpect(view().name("categoria/listado"))
                .andExpect(model().attribute("categorias", hasSize(2)))
                .andExpect(content().string(containsString("Limpieza")));
    }

    @Test
    void consultarCategoriaMuestraSusProductos() throws Exception {
        mvc.perform(get("/categoria/detalle/" + limpieza.getIdCategoria()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cloro 1 galón")));
        mvc.perform(get("/categoria/nuevo")).andExpect(status().isOk());
        mvc.perform(get("/categoria/modificar/" + limpieza.getIdCategoria()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("value=\"Limpieza\"")));
    }

    @Test
    void registrarCategoriaSeGuarda() throws Exception {
        mvc.perform(post("/categoria/guardar")
                .param("descripcion", "  Mascotas ")
                .param("activo", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/categoria/listado"))
                .andExpect(flash().attributeExists("todoOk"));

        em.flush();
        em.clear();
        assertTrue(categoriaRepository.findFirstByDescripcionIgnoreCase("Mascotas").isPresent());
    }

    @Test
    void noPermiteCategoriaDuplicadaNiVacia() throws Exception {
        mvc.perform(post("/categoria/guardar").param("descripcion", "limpieza"))
                .andExpect(status().isOk())
                .andExpect(view().name("categoria/formulario"))
                .andExpect(model().attributeExists("error"));

        mvc.perform(post("/categoria/guardar").param("descripcion", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("categoria/formulario"))
                .andExpect(model().attributeHasFieldErrors("categoria", "descripcion"));

        assertEquals(2, categoriaRepository.count());
    }

    @Test
    void editarCategoriaSeGuarda() throws Exception {
        mvc.perform(post("/categoria/guardar")
                .param("idCategoria", limpieza.getIdCategoria().toString())
                .param("descripcion", "Limpieza y desinfección")
                .param("activo", "true"))
                .andExpect(flash().attributeExists("todoOk"));

        em.flush();
        em.clear();
        assertEquals("Limpieza y desinfección",
                categoriaRepository.findById(limpieza.getIdCategoria()).orElseThrow().getDescripcion());
    }

    @Test
    void eliminarCategoriaSinProductos() throws Exception {
        mvc.perform(post("/categoria/eliminar").param("idCategoria", vacia.getIdCategoria().toString()))
                .andExpect(flash().attributeExists("todoOk"));

        em.flush();
        em.clear();
        assertFalse(categoriaRepository.existsById(vacia.getIdCategoria()));
    }

    @Test
    void eliminarCategoriaConProductosLaDesactiva() throws Exception {
        mvc.perform(post("/categoria/eliminar").param("idCategoria", limpieza.getIdCategoria().toString()))
                .andExpect(flash().attributeExists("todoOk"));

        em.flush();
        em.clear();
        Categoria c = categoriaRepository.findById(limpieza.getIdCategoria()).orElseThrow();
        assertFalse(c.getActivo());
    }

    // ---------------- Regresión: módulos existentes ----------------

    @Test
    void productosYCatalogoSiguenFuncionando() throws Exception {
        mvc.perform(get("/producto/listado"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cloro 1 galón")));
        mvc.perform(get("/catalogo"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Limpieza")));
        mvc.perform(get("/"))
                .andExpect(status().isOk());
    }
}
