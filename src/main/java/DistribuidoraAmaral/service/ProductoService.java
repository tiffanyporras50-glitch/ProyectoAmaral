package DistribuidoraAmaral.service;

import DistribuidoraAmaral.domain.Producto;
import java.util.List;

public interface ProductoService {

    public List<Producto> getProductos();

    public Producto getProducto(Integer idProducto);

    public void save(Producto producto);

    public void delete(Integer idProducto);

}
