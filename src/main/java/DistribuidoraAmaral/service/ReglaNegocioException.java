package DistribuidoraAmaral.service;

/**
 * Error de validación de negocio. Guarda la llave del mensaje (messages.properties)
 * para que el controlador lo muestre en el idioma seleccionado.
 *
 * @author Jezrael Hernández
 */
public class ReglaNegocioException extends RuntimeException {

    private final String llave;
    private final Object[] argumentos;

    public ReglaNegocioException(String llave, Object... argumentos) {
        super(llave);
        this.llave = llave;
        this.argumentos = argumentos;
    }

    public String getLlave() {
        return llave;
    }

    public Object[] getArgumentos() {
        return argumentos;
    }
}
