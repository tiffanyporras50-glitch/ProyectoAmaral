package DistribuidoraAmaral.service;

import DistribuidoraAmaral.domain.Usuario;
import DistribuidoraAmaral.repository.UsuarioRepository;
import java.util.List;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Usuario buscar(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("El usuario no existe"));
    }

    public void guardar(Usuario datos) {
        if (datos.getUsername() == null
                || datos.getUsername().isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre de usuario es obligatorio");
        }

        if (datos.getNombre() == null || datos.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        String username = datos.getUsername().trim();
        boolean nuevo = datos.getIdUsuario() == null;

        boolean repetido = nuevo
                ? usuarioRepository.findByUsername(username).isPresent()
                : usuarioRepository.existsByUsernameAndIdUsuarioNot(
                        username, datos.getIdUsuario());

        if (repetido) {
            throw new IllegalArgumentException(
                    "Ese nombre de usuario ya está registrado");
        }

        String password = datos.getPassword();
        boolean cambiarPassword =
                password != null && !password.isBlank();

        if (nuevo && !cambiarPassword) {
            throw new IllegalArgumentException(
                    "La contraseña es obligatoria");
        }

        if (cambiarPassword
                && (password.length() < 8
                || password.getBytes(java.nio.charset.StandardCharsets.UTF_8)
                        .length > 72)) {
            throw new IllegalArgumentException(
                    "La contraseña debe tener al menos 8 caracteres "
                    + "y no superar 72 bytes");
        }

        Usuario usuario = nuevo
                ? new Usuario() : buscar(datos.getIdUsuario());

        usuario.setUsername(username);
        usuario.setNombre(datos.getNombre().trim());
        usuario.setApellidos(datos.getApellidos());
        usuario.setNombreComercio(datos.getNombreComercio());
        usuario.setCorreo(datos.getCorreo());
        usuario.setTelefono(datos.getTelefono());
        usuario.setDireccion(datos.getDireccion());
        usuario.setActivo(datos.isActivo());

        if (cambiarPassword) {
            usuario.setPassword(passwordEncoder.encode(password));
        }

        usuarioRepository.save(usuario);
    }

    public void desactivar(Integer id) {
        Usuario usuario = buscar(id);
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }
}