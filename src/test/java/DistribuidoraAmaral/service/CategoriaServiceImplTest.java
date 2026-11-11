package DistribuidoraAmaral.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import DistribuidoraAmaral.domain.Categoria;
import DistribuidoraAmaral.repository.CategoriaRepository;
import DistribuidoraAmaral.repository.ProductoRepository;
import DistribuidoraAmaral.service.CategoriaService.ResultadoEliminacion;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias del servicio de categorías con Mockito.
 *
 * @author Jezrael Hernández
 */
class CategoriaServiceImplTest {

    private CategoriaRepository categoriaRepository;
    private ProductoRepository productoRepository;
    private CategoriaServiceImpl categoriaService;

    @BeforeEach
    void preparar() {
        categoriaRepository = mock(CategoriaRepository.class);
        productoRepository = mock(ProductoRepository.class);
        categoriaService = new CategoriaServiceImpl(categoriaRepository, productoRepository);
    }

    private Categoria categoria(Integer id, String descripcion) {
        Categoria c = new Categoria();
        c.setIdCategoria(id);
        c.setDescripcion(descripcion);
        return c;
    }

    @Test
    void registraCategoriaNuevaQuitandoEspacios() {
        Categoria nueva = categoria(null, "  Mascotas  ");
        nueva.setRutaImagen("  ");
        when(categoriaRepository.findFirstByDescripcionIgnoreCase("Mascotas")).thenReturn(Optional.empty());

        categoriaService.save(nueva);

        assertEquals("Mascotas", nueva.getDescripcion());
        assertNull(nueva.getRutaImagen());
        verify(categoriaRepository).saveAndFlush(nueva);
    }

    @Test
    void noPermiteNombreDuplicado() {
        when(categoriaRepository.findFirstByDescripcionIgnoreCase("limpieza"))
                .thenReturn(Optional.of(categoria(9, "Limpieza")));

        ReglaNegocioException e = assertThrows(ReglaNegocioException.class,
                () -> categoriaService.save(categoria(null, "limpieza")));

        assertEquals("categoria.error.duplicada", e.getLlave());
        verify(categoriaRepository, never()).saveAndFlush(any());
    }

    @Test
    void permiteEditarConservandoSuMismoNombre() {
        Categoria editada = categoria(9, "Limpieza");
        when(categoriaRepository.existsById(9)).thenReturn(true);
        when(categoriaRepository.findFirstByDescripcionIgnoreCase("Limpieza"))
                .thenReturn(Optional.of(categoria(9, "Limpieza")));

        categoriaService.save(editada);

        verify(categoriaRepository).saveAndFlush(editada);
    }

    @Test
    void nombreObligatorio() {
        ReglaNegocioException e = assertThrows(ReglaNegocioException.class,
                () -> categoriaService.save(categoria(null, "   ")));
        assertEquals("categoria.error.descripcionRequerida", e.getLlave());
    }

    @Test
    void eliminaCategoriaSinProductos() {
        Categoria c = categoria(20, "Temporal");
        when(categoriaRepository.findById(20)).thenReturn(Optional.of(c));
        when(productoRepository.countByCategoriaIdCategoria(20)).thenReturn(0L);

        assertEquals(ResultadoEliminacion.ELIMINADA, categoriaService.delete(20));
        verify(categoriaRepository).delete(c);
    }

    @Test
    void desactivaCategoriaConProductos() {
        Categoria c = categoria(9, "Limpieza");
        when(categoriaRepository.findById(9)).thenReturn(Optional.of(c));
        when(productoRepository.countByCategoriaIdCategoria(9)).thenReturn(2L);

        assertEquals(ResultadoEliminacion.DESACTIVADA, categoriaService.delete(9));
        assertFalse(c.getActivo());
        verify(categoriaRepository).save(c);
        verify(categoriaRepository, never()).delete(any());
    }

    @Test
    void eliminarCategoriaInexistente() {
        when(categoriaRepository.findById(77)).thenReturn(Optional.empty());
        assertEquals("categoria.error.noExiste", assertThrows(ReglaNegocioException.class,
                () -> categoriaService.delete(77)).getLlave());
    }
}
