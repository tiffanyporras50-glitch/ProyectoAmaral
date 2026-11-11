package DistribuidoraAmaral.repository;

import DistribuidoraAmaral.domain.Categoria;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository
        extends JpaRepository<Categoria, Integer> {


    public List<Categoria> findByActivoTrueOrderByDescripcionAsc();

    // CRUD de categorías: todas (activas e inactivas) en orden alfabético
    public List<Categoria> findAllByOrderByDescripcionAsc();

    // CRUD de categorías: para evitar nombres duplicados
    public Optional<Categoria> findFirstByDescripcionIgnoreCase(String descripcion);
}
