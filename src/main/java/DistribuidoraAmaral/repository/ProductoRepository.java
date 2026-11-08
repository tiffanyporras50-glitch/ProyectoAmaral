package DistribuidoraAmaral.repository;

import DistribuidoraAmaral.domain.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository
        extends JpaRepository<Producto, Integer> {

}
