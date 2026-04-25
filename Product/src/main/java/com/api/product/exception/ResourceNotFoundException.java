package com.api.product.exception;

/**
 * CUSTOM EXCEPTION - Excepción Personalizada
 * 
 * ¿PARA QUÉ SIRVE?
 * Esta es una excepción personalizada creada específicamente para cuando
 * un recurso (producto) no es encontrado en la base de datos.
 * 
 * ¿CÓMO FUNCIONA?
 * Extiende RuntimeException, lo que permite lanzarla en cualquier momento
 * sin declarar "throws" en los métodos.
 * 
 * EJEMPLO DE USO en ProductService:
 * if (!productRepository.existsById(id)) {
 *     throw new ResourceNotFoundException("Producto con ID " + id + " no encontrado");
 * }
 * 
 * Luego, GlobalExceptionHandler la captura automáticamente y devuelve 404.
 */
public class ResourceNotFoundException extends RuntimeException {
    
    /**
     * Constructor 1: Acepta solo un mensaje de error
     * 
     * EJEMPLO:
     * throw new ResourceNotFoundException("El producto no existe");
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor 2: Acepta un mensaje y una causa (otra excepción)
     * 
     * Sirve para encadenar excepciones y mantener el rastro del error original
     * 
     * EJEMPLO:
     * try {
     *     // código que lanza error
     * } catch (Exception e) {
     *     throw new ResourceNotFoundException("Producto no encontrado", e);
     * }
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
