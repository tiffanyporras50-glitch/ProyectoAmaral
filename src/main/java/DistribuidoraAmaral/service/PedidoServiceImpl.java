package DistribuidoraAmaral.service;

import DistribuidoraAmaral.domain.EstadoPedido;
import DistribuidoraAmaral.domain.Pedido;
import DistribuidoraAmaral.repository.PedidoRepository;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Jezrael Hernández
 */
@Service
public class PedidoServiceImpl implements PedidoService {

    // Largo máximo de la columna pedido.observacion
    private static final int MAX_OBSERVACION = 255;

    private final PedidoRepository pedidoRepository;

    // Inyección por constructor
    public PedidoServiceImpl(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> getPedidos(EstadoPedido estado) {
        if (estado == null) {
            return pedidoRepository.findAllByOrderByIdPedidoDesc();
        }
        return pedidoRepository.findByEstadoOrderByIdPedidoDesc(estado);
    }

    @Override
    @Transactional(readOnly = true)
    public Pedido getPedido(Integer idPedido) {
        if (idPedido == null) {
            return null;
        }
        return pedidoRepository.findById(idPedido).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<EstadoPedido, Long> getConteoPorEstado() {
        Map<EstadoPedido, Long> conteo = new EnumMap<>(EstadoPedido.class);
        for (EstadoPedido estado : EstadoPedido.values()) {
            conteo.put(estado, pedidoRepository.countByEstado(estado));
        }
        return conteo;
    }

    @Override
    @Transactional
    public void cambiarEstado(Integer idPedido, EstadoPedido nuevoEstado, String motivo) {
        Pedido pedido = getPedido(idPedido);
        if (pedido == null) {
            throw new ReglaNegocioException("pedido.error.noExiste", idPedido);
        }
        if (nuevoEstado == null) {
            throw new ReglaNegocioException("pedido.error.estadoRequerido");
        }

        EstadoPedido actual = pedido.getEstado();
        if (actual == nuevoEstado) {
            throw new ReglaNegocioException("pedido.error.mismoEstado", actual);
        }
        if (actual.isFinal()) {
            throw new ReglaNegocioException("pedido.error.estadoFinal", actual);
        }
        if (!actual.puedeCambiarA(nuevoEstado)) {
            throw new ReglaNegocioException("pedido.error.transicion", actual, nuevoEstado);
        }

        String texto = motivo == null ? "" : motivo.trim();
        if (nuevoEstado.isRequiereMotivo() && texto.isEmpty()) {
            throw new ReglaNegocioException("pedido.error.motivoRequerido", nuevoEstado);
        }

        // El motivo se agrega a la observación del pedido sin borrar la anterior
        if (!texto.isEmpty()) {
            String nota = "[" + nuevoEstado.name() + "] " + texto;
            String anterior = pedido.getObservacion();
            String observacion = (anterior == null || anterior.isBlank())
                    ? nota : anterior + " | " + nota;
            if (observacion.length() > MAX_OBSERVACION) {
                throw new ReglaNegocioException("pedido.error.motivoLargo");
            }
            pedido.setObservacion(observacion);
        }

        pedido.setEstado(nuevoEstado);
        pedidoRepository.save(pedido);
    }
}
