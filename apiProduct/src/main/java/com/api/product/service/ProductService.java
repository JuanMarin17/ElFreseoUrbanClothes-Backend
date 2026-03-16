package com.api.product.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.api.product.dto.ProductRequestDTO;
import com.api.product.dto.ProductResponseDTO;
import com.api.product.entity.Product;
import com.api.product.repository.ProductRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.RequiredArgsConstructor;
/**
 * Servicio para gestionar los productos.
 * Incluye métodos para crear, listar, actualizar y desactivar productos.
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final Cloudinary cloudinary;

  /**
     * Crea un nuevo producto en la base de datos.
     * Se valida que todos los campos requeridos estén presentes y que el producto no exista.
     * Si se envía una imagen, esta se sube a Cloudinary y se guarda la URL en el producto.
     *
     * @param productRequestDTO DTO con los datos del producto a crear (nombre, descripción, precios, stock, categoría y opcionalmente imagen)
     * @return ProductResponseDTO DTO con los datos del producto creado (incluye ID generado y URL de la imagen si aplica)
     * @throws RuntimeException si ocurre un error de validación o de subida de imagen
     */
    public ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO) {

        try {

            if (productRequestDTO == null ||
                productRequestDTO.getNameProduct() == null || productRequestDTO.getNameProduct().isBlank() ||
                productRequestDTO.getDescription_product() == null || productRequestDTO.getDescription_product().isBlank() ||
                productRequestDTO.getSale_price_product() == null || productRequestDTO.getSale_price_product() <= 0 ||
                productRequestDTO.getSupplier_price_product() == null || productRequestDTO.getSupplier_price_product() <= 0 ||
                productRequestDTO.getStock_product() == null || productRequestDTO.getStock_product() < 0) {

                throw new Exception("Todos los datos son requeridos");
            }

            boolean exists = productRepository
                    .existsByNameProductIgnoreCase(productRequestDTO.getNameProduct());

            if (exists) {
                throw new IllegalArgumentException("El producto ya existe");
            }

            Product product = new Product();

            product.setNameProduct(productRequestDTO.getNameProduct());
            product.setDescription_product(productRequestDTO.getDescription_product());
            product.setSize_product(productRequestDTO.getSize_product());
            product.setSale_price_product(productRequestDTO.getSale_price_product());
            product.setSupplier_price_product(productRequestDTO.getSupplier_price_product());
            product.setStock_product(productRequestDTO.getStock_product());
            product.setState_product(productRequestDTO.getState_product());
            product.setId_category(productRequestDTO.getId_category());
            product.setState_product(true); // activo por defecto

            // Subir imagen a Cloudinary 
            MultipartFile file = productRequestDTO.getImage();
            if (file != null && !file.isEmpty()) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> uploadResult = (Map<String, Object>) cloudinary.uploader().upload(file.getBytes(),
                            ObjectUtils.asMap("folder", "products"));
                    product.setImage_url((String) uploadResult.get("secure_url"));
                } catch (Exception e) {
                    throw new RuntimeException("Error al subir la imagen: " + e.getMessage());
                }
            }

            productRepository.save(product);

            ProductResponseDTO response = new ProductResponseDTO();

            response.setId_product(product.getId_product());
            response.setName_product(product.getNameProduct());
            response.setDescription_product(product.getDescription_product());
            response.setSize_product(product.getSize_product());
            response.setSale_price_product(product.getSale_price_product());
            response.setSupplier_price_product(product.getSupplier_price_product());
            response.setStock_product(product.getStock_product());
            response.setId_category(product.getId_category());

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Error al crear el producto: " + e.getMessage());
        }
    }
    
    /**
     * Lista todos los productos, tanto activos como inactivos.
     * Esto permite al frontend filtrar según el estado si se requiere.
     *
     * @return Lista de ProductResponseDTO con todos los productos existentes
     */
    public List<ProductResponseDTO> ListProducts() {

        List<Product> products = productRepository.findAll();
        List<ProductResponseDTO> list = new ArrayList<>();

        for (Product product : products) {

            ProductResponseDTO responseDTO = new ProductResponseDTO();

            responseDTO.setId_product(product.getId_product());
            responseDTO.setName_product(product.getNameProduct());
            responseDTO.setDescription_product(product.getDescription_product());
            responseDTO.setSize_product(product.getSize_product());
            responseDTO.setSale_price_product(product.getSale_price_product());
            responseDTO.setStock_product(product.getStock_product());
            product.setState_product(product.getState_product());
            responseDTO.setId_category(product.getId_category());

            // importante para que el frontend pueda filtrar
            responseDTO.setState_product(product.getState_product());

            list.add(responseDTO);
        }

        return list;
    }

    /**
     * Busca un producto por su ID.
     *
     * @param id_product ID del producto a buscar
     * @return Optional<ProductResponseDTO> con los datos del producto si existe, vacío si no se encuentra
     */
    public Optional<ProductResponseDTO> listId(Long id_product){

        Optional<Product> optionalProduct = productRepository.findById(id_product);

        if (optionalProduct.isPresent()) {

            Product product = optionalProduct.get();

            ProductResponseDTO responseDTO = new ProductResponseDTO();

            responseDTO.setId_product(product.getId_product());
            responseDTO.setName_product(product.getNameProduct());
            responseDTO.setDescription_product(product.getDescription_product());
            responseDTO.setSize_product(product.getSize_product());
            responseDTO.setSale_price_product(product.getSale_price_product());
            responseDTO.setSupplier_price_product(product.getSupplier_price_product());
            responseDTO.setStock_product(product.getStock_product());
            responseDTO.setState_product(product.getState_product());
            responseDTO.setId_category(product.getId_category());

            return Optional.of(responseDTO);

        } else {
            return Optional.empty();
        }
    }

    /**
     * Actualiza los datos de un producto existente.
     * Se puede actualizar nombre, descripción, tamaño, precios, stock, categoría y estado.
     * Si se envía una nueva imagen, esta se sube a Cloudinary y reemplaza la anterior.
     *
     * @param id_product ID del producto a actualizar
     * @param productRequestDTO DTO con los nuevos datos del producto
     * @return Optional<ProductResponseDTO> con los datos actualizados del producto si existe, vacío si no se encuentra
     */
        public Optional<ProductResponseDTO> updateProduct(Long id_product, ProductRequestDTO productRequestDTO){

        Optional<Product> optionalProduct = productRepository.findById(id_product);

        if (optionalProduct.isPresent()) {

            Product product = optionalProduct.get();

            product.setNameProduct(productRequestDTO.getNameProduct());
            product.setDescription_product(productRequestDTO.getDescription_product());
            product.setSize_product(productRequestDTO.getSize_product());
            product.setSale_price_product(productRequestDTO.getSale_price_product());
            product.setSupplier_price_product(productRequestDTO.getSupplier_price_product());
            product.setStock_product(productRequestDTO.getStock_product());
            product.setState_product(productRequestDTO.getState_product());
            product.setId_category(productRequestDTO.getId_category());

            // Subir imagen a Cloudinary si viene
            // Obtener la imagen del DTO enviado por el frontend
            MultipartFile file = productRequestDTO.getImage();

            // Verificar si el archivo no es nulo y no está vacío
            if (file != null && !file.isEmpty()) {
                try {
         // Subir la imagen a Cloudinary
        // cloudinary.uploader().upload() recibe los bytes de la imagen y un mapa de opciones
        // ObjectUtils.asMap("folder", "products") indica que la imagen se guardará en la carpeta "products" en Cloudinary
        // @SuppressWarnings("unchecked") sirve para callar esa advertencia y evitar que ensucie tu compilació
                    @SuppressWarnings("unchecked")
                    Map<String, Object> uploadResult = (Map<String, Object>) cloudinary.uploader().upload(file.getBytes(),
                            ObjectUtils.asMap("folder", "products"));
         // Guardar la URL segura (https) que devuelve Cloudinary en el campo image_url del producto
                    product.setImage_url((String) uploadResult.get("secure_url"));
                } catch (Exception e) {
                    throw new RuntimeException("Error al subir la imagen: " + e.getMessage());
                }
            }

            Product updateProduct = productRepository.save(product);

            ProductResponseDTO responseDTO = new ProductResponseDTO();

            responseDTO.setId_product(updateProduct.getId_product());
            responseDTO.setName_product(updateProduct.getNameProduct());
            responseDTO.setDescription_product(updateProduct.getDescription_product());
            responseDTO.setSize_product(updateProduct.getSize_product());
            responseDTO.setSale_price_product(updateProduct.getSale_price_product());
            responseDTO.setSupplier_price_product(updateProduct.getSupplier_price_product());
            responseDTO.setStock_product(updateProduct.getStock_product());
            product.setState_product(updateProduct.getState_product());
            responseDTO.setId_category(updateProduct.getId_category());

            return Optional.of(responseDTO);

        } else {
            return Optional.empty();
        }
    }

    /**
     * Desactiva un producto (cambia su estado a inactivo) según el ID enviado.
     * No elimina el registro de la base de datos.
     *
     * @param id_product ID del producto a desactivar
     * @return Optional<ProductResponseDTO> con los datos del producto desactivado si existe, vacío si no se encuentra
     */
    public Optional<ProductResponseDTO> inactiveProduct(Long id_product){

        Optional<Product> optionalProduct = productRepository.findById(id_product);

        if(optionalProduct.isPresent()){

            Product product = optionalProduct.get();

            //  delete -> producto inactivo
            product.setState_product(false);

            productRepository.save(product);

            ProductResponseDTO responseDTO = new ProductResponseDTO();

            responseDTO.setId_product(product.getId_product());
            responseDTO.setName_product(product.getNameProduct());
            responseDTO.setDescription_product(product.getDescription_product());
            responseDTO.setSize_product(product.getSize_product());
            responseDTO.setSale_price_product(product.getSale_price_product());
            responseDTO.setSupplier_price_product(product.getSupplier_price_product());
            responseDTO.setStock_product(product.getStock_product());
            product.setState_product(product.getState_product());
            responseDTO.setId_category(product.getId_category());

            return Optional.of(responseDTO);

        } else {
            return Optional.empty();
        }
    }
}