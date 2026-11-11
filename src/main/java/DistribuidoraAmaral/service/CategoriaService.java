package DistribuidoraAmaral.service;

import DistribuidoraAmaral.domain.Categoria;
import DistribuidoraAmaral.domain.Producto;
import java.util.List;
import java.util.Map;

/**
 * CRUD de categorías.
 *
 * @author Jezrael Hernández
 */
public interface CategoriaService {

    // Resultado al intentar eliminar una categoría
    public enum ResultadoEliminacion {
        ELIMINADA, DESACTIVADA
    }

    public List<Categoria> getCategorias();

    public Categoria getCategoria(Integer idCategoria);

    // Productos asociados a la categoría (activos e inactivos)
    public List<Producto> getProductos(Integer idCategoria);

    // Cantidad de productos por categoría: idCategoria -> cantidad
    public Map<Integer, Long> getConteoProductos();

    // Valida que el nombre no se repita y guarda la categoría
    public void save(Categoria categoria);

    // Se elimina si no tiene productos; si los tiene, solo se desactiva
    public ResultadoEliminacion delete(Integer idCategoria);
}
