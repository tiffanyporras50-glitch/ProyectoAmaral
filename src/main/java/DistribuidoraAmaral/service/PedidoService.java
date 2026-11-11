package DistribuidoraAmaral.service;

import DistribuidoraAmaral.domain.EstadoPedido;
import DistribuidoraAmaral.domain.Pedido;
import java.util.List;
import java.util.Map;

/**
 * HU 17: Gestionar pedidos.
 *
 * @author Jezrael Hernández
 */
public interface PedidoService {

    // Si estado es null se devuelven todos los pedidos
    public List<Pedido> getPedidos(EstadoPedido estado);

    public Pedido getPedido(Integer idPedido);

    // Cantidad de pedidos en cada estado (para los filtros del listado)
    public Map<EstadoPedido, Long> getConteoPorEstado();

    // Valida la transición y guarda el nuevo estado del pedido
    public void cambiarEstado(Integer idPedido, EstadoPedido nuevoEstado, String motivo);
}
