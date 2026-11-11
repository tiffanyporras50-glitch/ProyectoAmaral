package DistribuidoraAmaral.repository;

import DistribuidoraAmaral.domain.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsernameAndIdUsuarioNot(
            String username, Integer idUsuario);
}