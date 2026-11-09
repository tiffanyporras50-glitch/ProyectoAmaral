package DistribuidoraAmaral.domain;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "categoria")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Integer idCategoria;

    @Column(name = "descripcion", nullable = false, length = 50)
    private String descripcion;

    @Column(name = "ruta_imagen", length = 1024)
    private String rutaImagen;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
}
