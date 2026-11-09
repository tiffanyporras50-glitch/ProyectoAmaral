package DistribuidoraAmaral.controller;

import DistribuidoraAmaral.domain.Producto;
import DistribuidoraAmaral.repository.CategoriaRepository;
import DistribuidoraAmaral.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/producto")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @GetMapping("/listado")
    public String listado(Model model) {
        model.addAttribute("productos", productoService.getProductos());
        model.addAttribute("titulo", "Productos - Distribuidora Amaral");
        return "producto/listado";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("titulo", "Registrar producto");
        return "producto/formulario";
    }

    @GetMapping("/modificar/{idProducto}")
    public String modificar(@PathVariable Integer idProducto,
            Model model) {

        Producto producto = productoService.getProducto(idProducto);

        if (producto == null) {
            return "redirect:/producto/listado";
        }

        model.addAttribute("producto", producto);
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("titulo", "Modificar producto");
        return "producto/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Producto producto) {
        productoService.save(producto);
        return "redirect:/producto/listado";
    }

    @PostMapping("/eliminar/{idProducto}")
    public String eliminar(@PathVariable Integer idProducto) {
        if (productoService.getProducto(idProducto) != null) {
            productoService.delete(idProducto);
        }

        return "redirect:/producto/listado";
    }
}
