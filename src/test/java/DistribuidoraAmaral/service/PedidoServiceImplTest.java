package DistribuidoraAmaral.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import DistribuidoraAmaral.domain.EstadoPedido;
import DistribuidoraAmaral.domain.Pedido;
import DistribuidoraAmaral.repository.PedidoRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias del servicio de pedidos (HU 17) con Mockito.
 *
 * @author Jezrael Hernández
 */
class PedidoServiceImplTest {

    private PedidoRepository pedidoRepository;
    private PedidoServiceImpl pedidoService;

    @BeforeEach
    void preparar() {
        pedidoRepository = mock(PedidoRepository.class);
        pedidoService = new PedidoServiceImpl(pedidoRepository);
    }

    private Pedido pedido(int id, EstadoPedido estado, String observacion) {
        Pedido p = new Pedido();
        p.setIdPedido(id);
        p.setEstado(estado);
        p.setObservacion(observacion);
        when(pedidoRepository.findById(id)).thenReturn(Optional.of(p));
        return p;
    }

    @Test
    void confirmaUnPedidoRecibido() {
        Pedido p = pedido(3, EstadoPedido.RECIBIDO, null);

        pedidoService.cambiarEstado(3, EstadoPedido.CONFIRMADO, "");

        assertEquals(EstadoPedido.CONFIRMADO, p.getEstado());
        assertNull(p.getObservacion());
        verify(pedidoRepository).save(p);
    }

    @Test
    void rechazaTransicionIncorrecta() {
        Pedido p = pedido(3, EstadoPedido.RECIBIDO, null);

        ReglaNegocioException e = assertThrows(ReglaNegocioException.class,
                () -> pedidoService.cambiarEstado(3, EstadoPedido.ENTREGADO, null));

        assertEquals("pedido.error.transicion", e.getLlave());
        assertEquals(EstadoPedido.RECIBIDO, p.getEstado());
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void cancelarSinMotivoNoSePermite() {
        pedido(1, EstadoPedido.CONFIRMADO, null);

        ReglaNegocioException e = assertThrows(ReglaNegocioException.class,
                () -> pedidoService.cambiarEstado(1, EstadoPedido.CANCELADO, "   "));

        assertEquals("pedido.error.motivoRequerido", e.getLlave());
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void cancelarConMotivoLoAgregaALaObservacion() {
        Pedido p = pedido(1, EstadoPedido.CONFIRMADO, "Entregar en la mañana");

        pedidoService.cambiarEstado(1, EstadoPedido.CANCELADO, " Cliente sin fondos ");

        assertEquals(EstadoPedido.CANCELADO, p.getEstado());
        assertEquals("Entregar en la mañana | [CANCELADO] Cliente sin fondos", p.getObservacion());
        verify(pedidoRepository).save(p);
    }

    @Test
    void estadoFinalNoSePuedeCambiar() {
        pedido(5, EstadoPedido.ENTREGADO, null);

        ReglaNegocioException e = assertThrows(ReglaNegocioException.class,
                () -> pedidoService.cambiarEstado(5, EstadoPedido.CANCELADO, "x"));

        assertEquals("pedido.error.estadoFinal", e.getLlave());
    }

    @Test
    void mismoEstadoNoSePermite() {
        pedido(2, EstadoPedido.LISTO_DESPACHO, null);

        ReglaNegocioException e = assertThrows(ReglaNegocioException.class,
                () -> pedidoService.cambiarEstado(2, EstadoPedido.LISTO_DESPACHO, null));

        assertEquals("pedido.error.mismoEstado", e.getLlave());
    }

    @Test
    void pedidoInexistenteOEstadoNulo() {
        when(pedidoRepository.findById(99)).thenReturn(Optional.empty());
        assertEquals("pedido.error.noExiste", assertThrows(ReglaNegocioException.class,
                () -> pedidoService.cambiarEstado(99, EstadoPedido.CONFIRMADO, null)).getLlave());

        pedido(3, EstadoPedido.RECIBIDO, null);
        assertEquals("pedido.error.estadoRequerido", assertThrows(ReglaNegocioException.class,
                () -> pedidoService.cambiarEstado(3, null, null)).getLlave());
    }

    @Test
    void motivoDemasiadoLargoNoSeGuarda() {
        pedido(1, EstadoPedido.CONFIRMADO, "x".repeat(200));

        ReglaNegocioException e = assertThrows(ReglaNegocioException.class,
                () -> pedidoService.cambiarEstado(1, EstadoPedido.CANCELADO, "y".repeat(100)));

        assertEquals("pedido.error.motivoLargo", e.getLlave());
        verify(pedidoRepository, never()).save(any());
    }
}
