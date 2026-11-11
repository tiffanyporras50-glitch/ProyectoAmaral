package DistribuidoraAmaral.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Immutable;

/**
 * Vista de solo lectura de la tabla usuario con los datos de contacto que se
 * muestran en los pedidos (cliente y vendedor).
 *
 * Es @Immutable: desde aquí nunca se modifican usuarios. La administración de
 * usuarios (HU 02 a 05) corresponde a otro módulo.
 *
 * @author Jezrael Hernández
 */
@Data
@Entity
@Immutable
@Table(name = "usuario")
public class Cliente {

    @Id
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "username")
    private String username;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "apellidos")
    private String apellidos;

    @Column(name = "nombre_comercio")
    private String nombreComercio;

    @Column(name = "correo")
    private String correo;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "direccion")
    private String direccion;

    // Nombre y apellidos juntos para mostrar en las vistas
    public String getNombreCompleto() {
        return apellidos == null ? nombre : nombre + " " + apellidos;
    }
}
