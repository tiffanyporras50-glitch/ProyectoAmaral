package DistribuidoraAmaral.repository;

import DistribuidoraAmaral.domain.EstadoPedido;
import DistribuidoraAmaral.domain.Pedido;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Jezrael Hernández
 */
public interface PedidoRepository
        extends JpaRepository<Pedido, Integer> {

    // HU 17: todos los pedidos, los más recientes primero
    public List<Pedido> findAllByOrderByIdPedidoDesc();

    // HU 17: pedidos filtrados por estado
    public List<Pedido> findByEstadoOrderByIdPedidoDesc(EstadoPedido estado);

    public long countByEstado(EstadoPedido estado);
}
