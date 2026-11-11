package DistribuidoraAmaral.controller;

import DistribuidoraAmaral.service.CatalogoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Tiffany Porras
 */
@Controller
@RequestMapping("/catalogo")
public class CatalogoController {

    // Los atributos son final para asegurar la inmutabilidad
    private final CatalogoService catalogoService;

    // Inyección por constructor (no requiere @Autowired en Spring moderno)
    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    // HU 11: catálogo completo
    @GetMapping({"", "/"})
    public String catalogo(Model model) {
        model.addAttribute("productos", catalogoService.getProductos());
        model.addAttribute("categorias", catalogoService.getCategorias());
        return "/catalogo/listado";
    }

    // HU 11 y HU 12: catálogo filtrado por una categoría
    @GetMapping("/categoria/{idCategoria}")
    public String porCategoria(@PathVariable("idCategoria") Integer idCategoria, Model model) {
        model.addAttribute("productos", catalogoService.getProductosPorCategoria(idCategoria));
        model.addAttribute("categorias", catalogoService.getCategorias());
        model.addAttribute("idCategoriaActual", idCategoria); // para marcar la pestaña activa
        return "/catalogo/listado";
    }

    // HU 12: búsqueda por nombre o marca
    @GetMapping("/buscar")
    public String buscar(@RequestParam(name = "texto", required = false) String texto, Model model) {
        model.addAttribute("productos", catalogoService.buscar(texto));
        model.addAttribute("categorias", catalogoService.getCategorias());
        model.addAttribute("texto", texto); // para que el texto se mantenga en la caja de búsqueda
        return "/catalogo/listado";
    }
}
