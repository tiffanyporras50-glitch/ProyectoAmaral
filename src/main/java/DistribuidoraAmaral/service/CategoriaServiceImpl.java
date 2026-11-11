package DistribuidoraAmaral.service;

import DistribuidoraAmaral.domain.Categoria;
import DistribuidoraAmaral.domain.Producto;
import DistribuidoraAmaral.repository.CategoriaRepository;
import DistribuidoraAmaral.repository.ProductoRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Jezrael Hernández
 */
@Service
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    // Inyección por constructor
    public CategoriaServiceImpl(CategoriaRepository categoriaRepository,
            ProductoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> getCategorias() {
        return categoriaRepository.findAllByOrderByDescripcionAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public Categoria getCategoria(Integer idCategoria) {
        if (idCategoria == null) {
            return null;
        }
        return categoriaRepository.findById(idCategoria).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> getProductos(Integer idCategoria) {
        return productoRepository.findByCategoriaIdCategoriaOrderByNombreAsc(idCategoria);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Integer, Long> getConteoProductos() {
        Map<Integer, Long> conteo = new HashMap<>();
        for (Categoria c : categoriaRepository.findAll()) {
            conteo.put(c.getIdCategoria(),
                    productoRepository.countByCategoriaIdCategoria(c.getIdCategoria()));
        }
        return conteo;
    }

    @Override
    @Transactional
    public void save(Categoria categoria) {
        String descripcion = categoria.getDescripcion() == null ? "" : categoria.getDescripcion().trim();
        if (descripcion.isEmpty()) {
            throw new ReglaNegocioException("categoria.error.descripcionRequerida");
        }
        categoria.setDescripcion(descripcion);

        // La URL de la imagen es opcional: si viene vacía se guarda null
        if (categoria.getRutaImagen() != null && categoria.getRutaImagen().isBlank()) {
            categoria.setRutaImagen(null);
        }
        if (categoria.getActivo() == null) {
            categoria.setActivo(true);
        }

        // Si se está modificando, la categoría debe existir
        if (categoria.getIdCategoria() != null
                && !categoriaRepository.existsById(categoria.getIdCategoria())) {
            throw new ReglaNegocioException("categoria.error.noExiste", categoria.getIdCategoria());
        }

        // No se permiten dos categorías con el mismo nombre
        Optional<Categoria> existente = categoriaRepository.findFirstByDescripcionIgnoreCase(descripcion);
        if (existente.isPresent()
                && !existente.get().getIdCategoria().equals(categoria.getIdCategoria())) {
            throw new ReglaNegocioException("categoria.error.duplicada", descripcion);
        }

        try {
            categoriaRepository.saveAndFlush(categoria);
        } catch (DataIntegrityViolationException e) {
            // Respaldo: la columna descripcion es UNIQUE en MySQL
            // (por ejemplo "Ferreteria" y "Ferretería" se consideran iguales)
            throw new ReglaNegocioException("categoria.error.duplicada", descripcion);
        }
    }

    @Override
    @Transactional
    public ResultadoEliminacion delete(Integer idCategoria) {
        Categoria categoria = getCategoria(idCategoria);
        if (categoria == null) {
            throw new ReglaNegocioException("categoria.error.noExiste", idCategoria);
        }

        // Si tiene productos no se puede borrar (llave foránea): se desactiva
        if (productoRepository.countByCategoriaIdCategoria(idCategoria) > 0) {
            categoria.setActivo(false);
            categoriaRepository.save(categoria);
            return ResultadoEliminacion.DESACTIVADA;
        }

        categoriaRepository.delete(categoria);
        return ResultadoEliminacion.ELIMINADA;
    }
}
