package DistribuidoraAmaral.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @NotBlank(message = "El usuario es obligatorio")
    @Size(max = 30)
    @Column(nullable = false, unique = true, length = 30)
    private String username;

    @Column(nullable = false, length = 512)
    private String password;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String nombre;

    @Size(max = 80)
    @Column(length = 80)
    private String apellidos;

    @Size(max = 100)
    @Column(name = "nombre_comercio", length = 100)
    private String nombreComercio;

    @Email(message = "Escribe un correo válido")
    @Size(max = 75)
    @Column(length = 75)
    private String correo;

    @Size(max = 20)
    @Column(length = 20)
    private String telefono;

    @Size(max = 255)
    @Column(length = 255)
    private String direccion;

    @Column(nullable = false)
    private boolean activo = true;
}