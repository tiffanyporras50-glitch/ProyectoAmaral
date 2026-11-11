package DistribuidoraAmaral.domain;

import static DistribuidoraAmaral.domain.EstadoPedido.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Pruebas de las transiciones de estado de un pedido (HU 17).
 *
 * @author Jezrael Hernández
 */
class EstadoPedidoTest {

    @Test
    void recibidoSoloPuedeConfirmarseOCancelarse() {
        assertTrue(RECIBIDO.puedeCambiarA(CONFIRMADO));
        assertTrue(RECIBIDO.puedeCambiarA(CANCELADO));
        assertFalse(RECIBIDO.puedeCambiarA(LISTO_DESPACHO));
        assertFalse(RECIBIDO.puedeCambiarA(ENTREGADO));
        assertFalse(RECIBIDO.puedeCambiarA(NO_ENTREGADO));
        assertFalse(RECIBIDO.puedeCambiarA(RECIBIDO));
    }

    @Test
    void flujoCompletoHastaEntregado() {
        assertTrue(CONFIRMADO.puedeCambiarA(LISTO_DESPACHO));
        assertTrue(LISTO_DESPACHO.puedeCambiarA(ENTREGADO));
        assertTrue(LISTO_DESPACHO.puedeCambiarA(NO_ENTREGADO));
        assertFalse(LISTO_DESPACHO.puedeCambiarA(CANCELADO));
        assertFalse(LISTO_DESPACHO.puedeCambiarA(RECIBIDO));
    }

    @Test
    void noEntregadoPuedeReintentarseOCancelarse() {
        assertTrue(NO_ENTREGADO.puedeCambiarA(LISTO_DESPACHO));
        assertTrue(NO_ENTREGADO.puedeCambiarA(CANCELADO));
        assertFalse(NO_ENTREGADO.puedeCambiarA(ENTREGADO));
    }

    @Test
    void entregadoYCanceladoSonFinales() {
        assertTrue(ENTREGADO.isFinal());
        assertTrue(CANCELADO.isFinal());
        assertFalse(RECIBIDO.isFinal());
        for (EstadoPedido e : values()) {
            assertFalse(ENTREGADO.puedeCambiarA(e));
            assertFalse(CANCELADO.puedeCambiarA(e));
        }
        assertFalse(RECIBIDO.puedeCambiarA(null));
    }

    @Test
    void cancelarYNoEntregadoRequierenMotivo() {
        assertTrue(CANCELADO.isRequiereMotivo());
        assertTrue(NO_ENTREGADO.isRequiereMotivo());
        assertFalse(CONFIRMADO.isRequiereMotivo());
        assertFalse(ENTREGADO.isRequiereMotivo());
    }
}
