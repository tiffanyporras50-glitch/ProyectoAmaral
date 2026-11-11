package DistribuidoraAmaral.service;

import DistribuidoraAmaral.domain.Categoria;
import DistribuidoraAmaral.domain.Producto;
import DistribuidoraAmaral.repository.CategoriaRepository;
import DistribuidoraAmaral.repository.ProductoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Tiffany Porras
 */
@Service
public class CatalogoService {

    // Los atributos son final para asegurar la inmutabilidad
    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    // Inyección por constructor (no requiere @Autowired en Spring moderno)
    public CatalogoService(ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    // HU 11: todos los productos activos del catálogo
    @Transactional(readOnly = true)
    public List<Producto> getProductos() {
        return productoRepository.findByActivoTrueOrderByNombreAsc();
    }

    // HU 11 y HU 12: productos activos de una categoría
    @Transactional(readOnly = true)
    public List<Producto> getProductosPorCategoria(Integer idCategoria) {
        return productoRepository.findByActivoTrueAndCategoriaIdCategoriaOrderByNombreAsc(idCategoria);
    }

    // HU 12: buscar productos activos por nombre o marca
    @Transactional(readOnly = true)
    public List<Producto> buscar(String texto) {
        if (texto == null || texto.isBlank()) { // Si no escribió nada, se muestra todo
            return getProductos();
        }
        String t = texto.trim();
        return productoRepository
                .findByActivoTrueAndNombreContainingIgnoreCaseOrActivoTrueAndMarcaContainingIgnoreCaseOrderByNombreAsc(t, t);
    }

    // Categorías activas para el menú de filtros del catálogo
    @Transactional(readOnly = true)
    public List<Categoria> getCategorias() {
        return categoriaRepository.findByActivoTrueOrderByDescripcionAsc();
    }
}
