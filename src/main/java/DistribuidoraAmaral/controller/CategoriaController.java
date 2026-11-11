package DistribuidoraAmaral.controller;

import DistribuidoraAmaral.domain.Categoria;
import DistribuidoraAmaral.service.CategoriaService;
import DistribuidoraAmaral.service.CategoriaService.ResultadoEliminacion;
import DistribuidoraAmaral.service.ReglaNegocioException;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * CRUD de categorías (menú Administración > Categorías).
 *
 * @author Jezrael Hernández
 */
@Controller
@RequestMapping("/categoria")
public class CategoriaController {

    private final CategoriaService categoriaService;
    private final MessageSource messageSource;

    // Inyección por constructor
    public CategoriaController(CategoriaService categoriaService, MessageSource messageSource) {
        this.categoriaService = categoriaService;
        this.messageSource = messageSource;
    }

    @GetMapping("/listado")
    public String listado(Model model) {
        var categorias = categoriaService.getCategorias();
        model.addAttribute("categorias", categorias);
        model.addAttribute("conteo", categoriaService.getConteoProductos());
        model.addAttribute("totalCategorias", categorias.size());
        model.addAttribute("activas", categorias.stream().filter(c -> Boolean.TRUE.equals(c.getActivo())).count());
        return "categoria/listado";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("categoria", new Categoria());
        return "categoria/formulario";
    }

    @GetMapping("/detalle/{idCategoria}")
    public String detalle(@PathVariable("idCategoria") Integer idCategoria,
            Model model, RedirectAttributes redirectAttributes) {
        Categoria categoria = categoriaService.getCategoria(idCategoria);
        if (categoria == null) {
            redirectAttributes.addFlashAttribute("error", mensaje("categoria.error.noExiste", idCategoria));
            return "redirect:/categoria/listado";
        }
        model.addAttribute("categoria", categoria);
        model.addAttribute("productos", categoriaService.getProductos(idCategoria));
        return "categoria/detalle";
    }

    @GetMapping("/modificar/{idCategoria}")
    public String modificar(@PathVariable("idCategoria") Integer idCategoria,
            Model model, RedirectAttributes redirectAttributes) {
        Categoria categoria = categoriaService.getCategoria(idCategoria);
        if (categoria == null) {
            redirectAttributes.addFlashAttribute("error", mensaje("categoria.error.noExiste", idCategoria));
            return "redirect:/categoria/listado";
        }
        model.addAttribute("categoria", categoria);
        return "categoria/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("categoria") Categoria categoria,
            BindingResult bindingResult, Model model,
            RedirectAttributes redirectAttributes) {
        // Si faltan datos se vuelve a mostrar el formulario con los errores
        if (bindingResult.hasErrors()) {
            return "categoria/formulario";
        }
        boolean nueva = categoria.getIdCategoria() == null;
        try {
            categoriaService.save(categoria);
        } catch (ReglaNegocioException e) {
            model.addAttribute("error", mensaje(e.getLlave(), e.getArgumentos()));
            return "categoria/formulario";
        }
        redirectAttributes.addFlashAttribute("todoOk",
                mensaje(nueva ? "categoria.mensaje.creada" : "categoria.mensaje.actualizada",
                        categoria.getDescripcion()));
        return "redirect:/categoria/listado";
    }

    // Recibe el id desde la ventana modal de confirmación (rutinas.js)
    @PostMapping("/eliminar")
    public String eliminar(@RequestParam("idCategoria") Integer idCategoria,
            RedirectAttributes redirectAttributes) {
        try {
            Categoria categoria = categoriaService.getCategoria(idCategoria);
            String nombre = categoria != null ? categoria.getDescripcion() : String.valueOf(idCategoria);
            ResultadoEliminacion resultado = categoriaService.delete(idCategoria);
            if (resultado == ResultadoEliminacion.ELIMINADA) {
                redirectAttributes.addFlashAttribute("todoOk", mensaje("categoria.mensaje.eliminada", nombre));
            } else {
                redirectAttributes.addFlashAttribute("todoOk", mensaje("categoria.mensaje.desactivada", nombre));
            }
        } catch (ReglaNegocioException e) {
            redirectAttributes.addFlashAttribute("error", mensaje(e.getLlave(), e.getArgumentos()));
        }
        return "redirect:/categoria/listado";
    }

    // Busca el texto en messages.properties según el idioma actual
    private String mensaje(String llave, Object... argumentos) {
        Object[] args = argumentos == null ? new Object[0] : argumentos.clone();
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Number n) {
                args[i] = String.valueOf(n); // evita que el número salga como "1,234"
            }
        }
        return messageSource.getMessage(llave, args, llave, LocaleContextHolder.getLocale());
    }
}
