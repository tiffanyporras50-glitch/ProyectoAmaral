package DistribuidoraAmaral.repository;

import DistribuidoraAmaral.domain.Producto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository
        extends JpaRepository<Producto, Integer> {

  
    public List<Producto> findByActivoTrueOrderByNombreAsc();

    public List<Producto> findByActivoTrueAndCategoriaIdCategoriaOrderByNombreAsc(Integer idCategoria);

  
    public List<Producto> findByActivoTrueAndNombreContainingIgnoreCaseOrActivoTrueAndMarcaContainingIgnoreCaseOrderByNombreAsc(
            String nombre, String marca);
}
