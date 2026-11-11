package DistribuidoraAmaral.controller;

import DistribuidoraAmaral.domain.EstadoPedido;
import DistribuidoraAmaral.domain.Pedido;
import DistribuidoraAmaral.service.PedidoService;
import DistribuidoraAmaral.service.ReglaNegocioException;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * HU 17: el administrador consulta los pedidos, los confirma o cancela y
 * actualiza su estado.
 *
 * @author Jezrael Hernández
 */
@Controller
@RequestMapping("/pedido")
public class PedidoController {

    private final PedidoService pedidoService;
    private final MessageSource messageSource;

    // Inyección por constructor
    public PedidoController(PedidoService pedidoService, MessageSource messageSource) {
        this.pedidoService = pedidoService;
        this.messageSource = messageSource;
    }

    // Lista de pedidos, opcionalmente filtrada por estado (?estado=RECIBIDO)
    @GetMapping("/listado")
    public String listado(@RequestParam(name = "estado", required = false) String estado,
            Model model) {
        EstadoPedido filtro = convertirEstado(estado);
        model.addAttribute("pedidos", pedidoService.getPedidos(filtro));
        model.addAttribute("conteo", pedidoService.getConteoPorEstado());
        model.addAttribute("estados", EstadoPedido.values());
        model.addAttribute("estadoActual", filtro);
        return "pedido/listado";
    }

    // Información completa del pedido y formulario para cambiar el estado
    @GetMapping("/detalle/{idPedido}")
    public String detalle(@PathVariable("idPedido") Integer idPedido,
            Model model, RedirectAttributes redirectAttributes) {
        Pedido pedido = pedidoService.getPedido(idPedido);
        if (pedido == null) {
            redirectAttributes.addFlashAttribute("error",
                    mensaje("pedido.error.noExiste", idPedido));
            return "redirect:/pedido/listado";
        }
        model.addAttribute("pedido", pedido);
        return "pedido/detalle";
    }

    // Cambia el estado del pedido validando que la transición sea correcta
    @PostMapping("/estado")
    public String cambiarEstado(@RequestParam("idPedido") Integer idPedido,
            @RequestParam(name = "estado", required = false) String estado,
            @RequestParam(name = "motivo", required = false) String motivo,
            @RequestParam(name = "origen", required = false) String origen,
            RedirectAttributes redirectAttributes) {
        try {
            EstadoPedido nuevo = convertirEstado(estado);
            pedidoService.cambiarEstado(idPedido, nuevo, motivo);
            redirectAttributes.addFlashAttribute("todoOk",
                    mensaje("pedido.mensaje.estadoActualizado", idPedido, nuevo));
        } catch (ReglaNegocioException e) {
            redirectAttributes.addFlashAttribute("error",
                    mensaje(e.getLlave(), e.getArgumentos()));
            // Se conserva el motivo escrito para no perderlo
            redirectAttributes.addFlashAttribute("motivo", motivo);
        }
        if ("listado".equals(origen)) {
            return "redirect:/pedido/listado";
        }
        return "redirect:/pedido/detalle/" + idPedido;
    }

    // Convierte el texto recibido a un estado; si no es válido devuelve null
    private EstadoPedido convertirEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            return null;
        }
        try {
            return EstadoPedido.valueOf(estado.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // Busca el texto en messages.properties según el idioma actual.
    // Los estados se traducen antes de insertarlos en el mensaje.
    private String mensaje(String llave, Object... argumentos) {
        Locale locale = LocaleContextHolder.getLocale();
        Object[] args = argumentos == null ? new Object[0] : argumentos.clone();
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof EstadoPedido e) {
                args[i] = messageSource.getMessage("pedido.estado." + e.name(), null, e.name(), locale);
            } else if (args[i] instanceof Number n) {
                args[i] = String.valueOf(n); // evita que el número salga como "1,234"
            }
        }
        return messageSource.getMessage(llave, args, llave, locale);
    }
}
