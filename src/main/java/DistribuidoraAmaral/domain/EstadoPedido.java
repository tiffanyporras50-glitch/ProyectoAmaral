package DistribuidoraAmaral.domain;

import java.util.List;

/**
 * Estados de un pedido (columna pedido.estado) y las transiciones permitidas
 * para el administrador (HU 17).
 *
 * RECIBIDO -> CONFIRMADO | CANCELADO
 * CONFIRMADO -> LISTO_DESPACHO | CANCELADO
 * LISTO_DESPACHO -> ENTREGADO | NO_ENTREGADO
 * NO_ENTREGADO -> LISTO_DESPACHO (reintento) | CANCELADO
 * ENTREGADO y CANCELADO son estados finales.
 *
 * @author Jezrael Hernández
 */
public enum EstadoPedido {

    RECIBIDO("primary"),
    CONFIRMADO("info"),
    LISTO_DESPACHO("warning"),
    ENTREGADO("success"),
    NO_ENTREGADO("danger"),
    CANCELADO("secondary");

    // Color de Bootstrap para la etiqueta (badge) del estado
    private final String color;

    EstadoPedido(String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    // Estados a los que se puede pasar desde el estado actual
    public List<EstadoPedido> getSiguientes() {
        return switch (this) {
            case RECIBIDO -> List.of(CONFIRMADO, CANCELADO);
            case CONFIRMADO -> List.of(LISTO_DESPACHO, CANCELADO);
            case LISTO_DESPACHO -> List.of(ENTREGADO, NO_ENTREGADO);
            case NO_ENTREGADO -> List.of(LISTO_DESPACHO, CANCELADO);
            case ENTREGADO, CANCELADO -> List.of();
        };
    }

    public boolean puedeCambiarA(EstadoPedido nuevo) {
        return nuevo != null && getSiguientes().contains(nuevo);
    }

    public boolean isFinal() {
        return getSiguientes().isEmpty();
    }

    // Para cancelar o reportar que no se entregó se debe indicar el motivo
    public boolean isRequiereMotivo() {
        return this == CANCELADO || this == NO_ENTREGADO;
    }
}
