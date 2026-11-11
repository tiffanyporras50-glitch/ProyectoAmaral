package DistribuidoraAmaral.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Entity
@Table(name = "categoria")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Integer idCategoria;

    // Validaciones del CRUD de categorías (el nombre es obligatorio y único)
    @NotBlank(message = "{categoria.error.descripcionRequerida}")
    @Size(max = 50, message = "{categoria.error.descripcionLarga}")
    @Column(name = "descripcion", nullable = false, length = 50, unique = true)
    private String descripcion;

    @Size(max = 1024, message = "{categoria.error.rutaLarga}")
    @Column(name = "ruta_imagen", length = 1024)
    private String rutaImagen;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
}
