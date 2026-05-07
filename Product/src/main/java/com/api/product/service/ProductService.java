package com.api.product.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.product.dto.ProductRequestDTO;
import com.api.product.dto.ProductResponseDTO;
import com.api.product.dto.StockAlertDTO;
import com.api.product.entity.Brand;
import com.api.product.entity.Category;
import com.api.product.entity.Product;
import com.api.product.entity.ProductImage;
import com.api.product.entity.ProductVariant;
import com.api.product.repository.BrandRepository;
import com.api.product.repository.CategoryRepository;
import com.api.product.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

/**
 * Servicio para gestionar productos.
 * Incluye métodos para crear, actualizar, listar, buscar, activar, desactivar y paginar productos.
 * También sube las imágenes a Cloudinary automáticamente.
 * 
 * Adicionalmente implementa alertas SSE (Server-Sent Events) cuando el stock llega
 * o baja del mínimo definido por el emprendedor.
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final CloudinaryUploadService cloudinaryUploadService;
    private final StockAlertService stockAlertService;

    /**
     * Crea un nuevo producto en la base de datos.
     * Valida campos obligatorios, crea variantes, imágenes y categorías.
     * Las imágenes se suben a Cloudinary automáticamente.
     * 
     * También verifica si alguna variante está en stock mínimo y dispara una alerta SSE.
     *
     * @param dto DTO con los datos del producto a crear
     * @return ProductResponseDTO con los datos del producto creado
     */
    @Transactional
    public ProductResponseDTO createProduct(ProductRequestDTO dto) {
        try {
            validateProductRequest(dto);

            // Verificar si existe producto con mismo nombre
            if (productRepository.existsByNameIgnoreCase(dto.getName())) {
                throw new RuntimeException("El producto ya existe");
            }

            // Buscar marca si existe
            Brand brand = findBrand(dto.getBrandId());

            // Crear producto
            Product product = Product.builder()
                    .name(dto.getName())
                    .description(dto.getDescription())
                    .brand(brand)
                    .active(true) // al crearse, siempre activo
                    .build();

            // Crear variantes
            product.setVariants(buildVariants(dto, product));

            // Subir imágenes a Cloudinary y guardar URLs
            product.setImages(buildImages(dto, product));

            // Asignar categorías
            product.setCategories(buildCategories(dto));

            // Guardar producto
            Product savedProduct = productRepository.save(product);

            // ALERTA SSE: si stock <= minStock
            checkAndSendStockAlerts(savedProduct);

            return mapToResponse(savedProduct);

        } catch (RuntimeException e) {
            throw new RuntimeException("Error al crear producto: " + e.getMessage());
        }
    }

    /**
     * Actualiza un producto existente.
     *
     * @param id  ID del producto
     * @param dto DTO con los datos actualizados
     * @return Optional con ProductResponseDTO actualizado si existe
     */
    @Transactional
    public Optional<ProductResponseDTO> updateProduct(UUID id, ProductRequestDTO dto) {

        Optional<Product> optional = productRepository.findById(id);
        if (optional.isEmpty())
            return Optional.empty();

        Product product = optional.get();

        try {
            // Validaciones
            validateProductRequest(dto);

            // Validar nombre repetido
            if (!product.getName().equalsIgnoreCase(dto.getName()) &&
                    productRepository.existsByNameIgnoreCase(dto.getName())) {
                throw new RuntimeException("Ya existe otro producto con el mismo nombre");
            }

            product.setName(dto.getName());
            product.setDescription(dto.getDescription());

            // Marca
            product.setBrand(findBrand(dto.getBrandId()));

            // Variantes
            product.setVariants(buildVariants(dto, product));

            // Imágenes
            // Imágenes (solo si llegan nuevas imágenes)
            if (dto.getImages() != null && !dto.getImages().isEmpty()) {

                // Agregar nuevas imágenes sin borrar las anteriores
                List<ProductImage> newImages = buildImages(dto, product);

                if (product.getImages() == null) {
                    product.setImages(new ArrayList<>());
                }

                product.getImages().addAll(newImages);
            }
            // Categorías
            product.setCategories(buildCategories(dto));

            Product savedProduct = productRepository.save(product);

            // ALERTA SSE: si stock <= minStock
            checkAndSendStockAlerts(savedProduct);

            return Optional.of(mapToResponse(savedProduct));

        } catch (RuntimeException e) {
            throw new RuntimeException("Error al actualizar producto: " + e.getMessage());
        }
    }

    /**
     * Lista productos con paginación.
     *
     * @param page número de página
     * @param size cantidad de registros por página
     * @return lista paginada de productos
     */
    @Transactional(readOnly=true)
    public List<ProductResponseDTO> listProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findAll(pageable);

        return productPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lista solo productos activos con paginación.
     *
     * @param page número de página
     * @param size cantidad de registros por página
     * @return lista paginada de productos activos
     */
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> listActiveProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findByActiveTrue(pageable);

        return productPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Busca producto por ID.
     *
     * @param id UUID del producto
     * @return Optional con producto si existe
     */
    @Transactional(readOnly = true)
    public Optional<ProductResponseDTO> getById(UUID id) {
        return productRepository.findById(id)
                .map(this::mapToResponse);
    }

    /**
     * Desactiva un producto.
     *
     * @param id UUID del producto
     * @return Optional con producto desactivado si existe
     */
    @Transactional
    public Optional<ProductResponseDTO> inactiveProduct(UUID id) {
        Optional<Product> optional = productRepository.findById(id);
        if (optional.isEmpty())
            return Optional.empty();

        Product product = optional.get();
        product.setActive(false);

        productRepository.save(product);
        return Optional.of(mapToResponse(product));
    }

    /**
     * Activa un producto.
     *
     * @param id UUID del producto
     * @return Optional con producto activado si existe
     */
    @Transactional
    public Optional<ProductResponseDTO> activeProduct(UUID id) {
        Optional<Product> optional = productRepository.findById(id);
        if (optional.isEmpty())
            return Optional.empty();

        Product product = optional.get();
        product.setActive(true);

        productRepository.save(product);
        return Optional.of(mapToResponse(product));
    }

    /**
     * Listar todos los productos sin restricción.
     *
     * @return lista completa de productos
     */
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> listAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Listar productos que únicamente estén activos en el sistema.
     *
     * @return lista completa de productos activos
     */
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> listAllActiveProducts() {
        return productRepository.findByActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Listar todos los productos que se hayan creado en un lapso de tiempo de 7 días.
     *
     * @return lista de productos nuevos
     */
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> listNewProducts() {
        OffsetDateTime oneWeekAgo = OffsetDateTime.now().minusDays(7);

        return productRepository.findByCreatedAtAfter(oneWeekAgo).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Listar los productos nuevos pero que además estén activos en el momento.
     *
     * @return lista de productos nuevos activos
     */
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> listNewActiveProducts() {
        OffsetDateTime oneWeekAgo = OffsetDateTime.now().minusDays(7);

        return productRepository.findByActiveTrueAndCreatedAtAfter(oneWeekAgo).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Valida campos obligatorios y consistencia del ProductRequestDTO.
     *
     * @param dto DTO a validar
     */
    private void validateProductRequest(ProductRequestDTO dto) {

        if (dto == null ||
                dto.getName() == null || dto.getName().isBlank() ||
                dto.getVariants() == null || dto.getVariants().isEmpty()) {

            throw new RuntimeException("Datos obligatorios faltantes");
        }

        dto.getVariants().forEach(v -> {

            if (v.getSku() == null || v.getSku().isBlank()) {
                throw new RuntimeException("SKU inválido en variantes");
            }

            if (v.getPrice() == null || v.getPrice().doubleValue() <= 0) {
                throw new RuntimeException("Precio inválido en variantes");
            }

            if (v.getStock() == null || v.getStock() < 0) {
                throw new RuntimeException("Stock inválido en variantes");
            }

            if (v.getMinStock() == null || v.getMinStock() < 0) {
                throw new RuntimeException("minStock inválido en variantes");
            }
        });
    }

    /**
     * Busca y retorna la marca asociada a un producto.
     *
     * @param brandId UUID de la marca
     * @return Brand si existe, o null si no se envió brandId
     */
    private Brand findBrand(UUID brandId) {
        if (brandId == null)
            return null;

        return brandRepository.findById(brandId)
                .orElseThrow(() -> new RuntimeException("Marca no encontrada"));
    }

    /**
     * Construye la lista de variantes del producto.
     *
     * @param dto DTO de entrada
     * @param product producto padre
     * @return lista de variantes
     */
    private List<ProductVariant> buildVariants(ProductRequestDTO dto, Product product) {

        return dto.getVariants().stream()
                .map(v -> ProductVariant.builder()
                        .sku(v.getSku())
                        .price(v.getPrice())
                        .stock(v.getStock())
                        .minStock(v.getMinStock())
                        .product(product)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Construye la lista de imágenes del producto.
     * Si existen imágenes, se suben automáticamente a Cloudinary.
     *
     * @param dto DTO de entrada
     * @param product producto padre
     * @return lista de imágenes
     */
    private List<ProductImage> buildImages(ProductRequestDTO dto, Product product) {

        List<ProductImage> images = new ArrayList<>();

        if (dto.getImages() == null || dto.getImages().isEmpty()) {
            return images;
        }

        dto.getImages().forEach(img -> {
                String url = cloudinaryUploadService.uploadProductImage(img);
            ProductImage image = ProductImage.builder()
                    .url(url)
                    .product(product)
                    .build();

            images.add(image);
        });

        return images;
    }

    /**
     * Construye la lista de categorías del producto a partir de los IDs enviados.
     *
     * @param dto DTO de entrada
     * @return lista de categorías
     */
    private List<Category> buildCategories(ProductRequestDTO dto) {

        if (dto.getCategoryIds() == null || dto.getCategoryIds().isEmpty()) {
            return new ArrayList<>();
        }

        List<Category> categories = categoryRepository.findAllById(dto.getCategoryIds());

        if (categories.isEmpty()) {
            throw new RuntimeException("Categorías no encontradas");
        }

        return categories;
    }

    /**
     * Verifica si el producto tiene variantes en stock bajo.
     * Si el stock es menor o igual al mínimo definido, se dispara una alerta SSE.
     *
     * @param product producto guardado
     */
    private void checkAndSendStockAlerts(Product product) {

        if (product.getVariants() == null || product.getVariants().isEmpty()) {
            return;
        }

        product.getVariants().forEach(variant -> {

            if (variant.getStock() <= variant.getMinStock()) {

                stockAlertService.sendAlert(
                        StockAlertDTO.builder()
                                .productId(product.getProductId())
                                .productName(product.getName())
                                .variantId(variant.getVariantId())
                                .sku(variant.getSku())
                                .stock(variant.getStock())
                                .minStock(variant.getMinStock())
                                .message("⚠️ Stock bajo: " + product.getName()
                                        + " (SKU: " + variant.getSku() + ")")
                                .timestamp(OffsetDateTime.now())
                                .build()
                );
            }
        });
    }



    /**
     * Convierte entidad Product a ProductResponseDTO.
     *
     * @param product entidad Product
     * @return ProductResponseDTO
     */
    private ProductResponseDTO mapToResponse(Product product) {

        List<ProductResponseDTO.VariantDTO> variants = product.getVariants().stream()
                .map(v -> ProductResponseDTO.VariantDTO.builder()
                        .sku(v.getSku())
                        .price(v.getPrice())
                        .stock(v.getStock())
                        .minStock(v.getMinStock())
                        .build())
                .collect(Collectors.toList());

        List<ProductResponseDTO.ImageDTO> images = product.getImages().stream()
                .map(i -> ProductResponseDTO.ImageDTO.builder()
                        .url(i.getUrl())
                        .build())
                .collect(Collectors.toList());

        List<String> categories = product.getCategories() != null
                ? product.getCategories().stream().map(Category::getName).collect(Collectors.toList())
                        : new ArrayList<>();

        return ProductResponseDTO.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .description(product.getDescription())
                .brandName(product.getBrand() != null ? product.getBrand().getName() : null)
                .createdAt(product.getCreatedAt())
                .status(product.getActive() ? "ACTIVE" : "INACTIVE")
                .variants(variants)
                .images(images)
                .categories(categories)
                .build();
    }
}