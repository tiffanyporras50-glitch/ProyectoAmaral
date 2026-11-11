package DistribuidoraAmaral.controller;

import DistribuidoraAmaral.domain.Usuario;
import DistribuidoraAmaral.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @InitBinder("usuario")
    public void configurarFormulario(WebDataBinder binder) {
        binder.setAllowedFields(
                "username", "password", "nombre", "apellidos",
                "nombreComercio", "correo", "telefono",
                "direccion", "activo");
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listar());
        return "usuario/listado";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "usuario/formulario";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("usuario", obtener(id));
        return "usuario/detalle";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable("id") Integer id, Model model) {
        Usuario formulario = new Usuario();
        BeanUtils.copyProperties(obtener(id), formulario, "password");
        model.addAttribute("usuario", formulario);
        return "usuario/formulario";
    }

    @PostMapping("/guardar")
    public String crear(
            @Valid @ModelAttribute("usuario") Usuario usuario,
            BindingResult resultado,
            RedirectAttributes mensajes) {
        usuario.setIdUsuario(null);
        return guardarFormulario(usuario, resultado, mensajes);
    }

    @PostMapping("/{id}/guardar")
    public String actualizar(
            @PathVariable("id") Integer id,
            @Valid @ModelAttribute("usuario") Usuario usuario,
            BindingResult resultado,
            RedirectAttributes mensajes) {
        obtener(id);
        usuario.setIdUsuario(id);
        return guardarFormulario(usuario, resultado, mensajes);
    }

    private String guardarFormulario(
            Usuario usuario,
            BindingResult resultado,
            RedirectAttributes mensajes) {
        if (resultado.hasErrors()) {
            usuario.setPassword(null);
            return "usuario/formulario";
        }

        try {
            usuarioService.guardar(usuario);
        } catch (IllegalArgumentException ex) {
            resultado.reject("usuario", ex.getMessage());
            usuario.setPassword(null);
            return "usuario/formulario";
        } catch (DataIntegrityViolationException ex) {
            resultado.reject("usuario",
                    "No se pudo guardar. Revisa los datos "
                    + "y que el nombre de usuario no esté repetido.");
            usuario.setPassword(null);
            return "usuario/formulario";
        }

        mensajes.addFlashAttribute("mensaje",
                "Usuario guardado correctamente");
        return "redirect:/usuarios";
    }

    @PostMapping("/{id}/desactivar")
    public String desactivar(
            @PathVariable("id") Integer id,
            RedirectAttributes mensajes) {
        obtener(id);
        usuarioService.desactivar(id);
        mensajes.addFlashAttribute("mensaje",
                "Usuario desactivado correctamente");
        return "redirect:/usuarios";
    }

    private Usuario obtener(Integer id) {
        try {
            return usuarioService.buscar(id);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "El usuario no existe");
        }
    }
}