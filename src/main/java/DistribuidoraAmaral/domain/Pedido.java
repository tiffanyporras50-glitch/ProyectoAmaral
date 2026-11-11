package DistribuidoraAmaral.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Tabla transaccional principal: pedido (HU 13 a 21).
 *
 * @author Jezrael Hernández
 */
@Data
@Entity
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Integer idPedido;

    // El cliente de un pedido no cambia una vez registrado
    @ManyToOne
    @JoinColumn(name = "id_cliente", nullable = false, updatable = false)
    private Cliente cliente;

    // Solo si lo registró un vendedor (HU 16)
    @ManyToOne
    @JoinColumn(name = "id_vendedor", updatable = false)
    private Cliente vendedor;

    @Column(name = "fecha", updatable = false)
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoPedido estado = EstadoPedido.RECIBIDO;

    @Column(name = "metodo_pago", nullable = false, length = 20)
    private String metodoPago;

    @Column(name = "forma_entrega", nullable = false, length = 20)
    private String formaEntrega;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "iva", nullable = false, precision = 12, scale = 2)
    private BigDecimal iva = BigDecimal.ZERO;

    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "observacion", length = 255)
    private String observacion;

    // Productos del pedido (tabla detalle_pedido)
    @OneToMany(mappedBy = "pedido")
    @OrderBy("idDetalle ASC")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<DetallePedido> detalles = new ArrayList<>();

    // Si no se indica la fecha, se usa la fecha y hora actual
    @PrePersist
    public void prePersist() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }
}
