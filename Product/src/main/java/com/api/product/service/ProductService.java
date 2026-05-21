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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.api.product.dto.ProductRequestDTO;
import com.api.product.dto.ProductResponseDTO;
import com.api.product.dto.StockAlertDTO;

import com.api.product.entity.Brand;
import com.api.product.entity.Category;
import com.api.product.entity.Product;
import com.api.product.entity.ProductImage;
import com.api.product.entity.ProductVariant;

import com.api.product.exception.BadRequestException;
import com.api.product.exception.ConflictException;
import com.api.product.exception.ResourceNotFoundException;

import com.api.product.repository.BrandRepository;
import com.api.product.repository.CategoryRepository;
import com.api.product.repository.ProductRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final StockAlertService stockAlertService;

    /**
     * Crea un nuevo producto en la base de datos.
     * Valida campos obligatorios, crea variantes, imágenes y categorías.
     *
     * Las imágenes se reciben como URLs (subidas previamente desde el frontend a Cloudinary).
     * Se permite un máximo de 6 imágenes por producto.
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

            if (productRepository.existsByNameIgnoreCase(dto.getName())) {
                throw new ConflictException("Ya existe un producto con ese nombre");
            }

            Brand brand = findBrand(dto.getBrandId());

            Product product = Product.builder()
                    .name(dto.getName())
                    .description(dto.getDescription())
                    .brand(brand)
                    .active(true)
                    .build();

            product.setVariants(buildVariants(dto, product));
            product.setImages(buildImages(dto, product));
            product.setCategories(buildCategories(dto));

            Product savedProduct = productRepository.save(product);

            sendStockAlertsIfNeeded(savedProduct);

            return mapToResponse(savedProduct);

        } catch (BadRequestException | ConflictException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error al crear producto: " + e.getMessage(), e);
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
            validateProductRequest(dto);

            if (!product.getName().equalsIgnoreCase(dto.getName()) &&
                    productRepository.existsByNameIgnoreCase(dto.getName())) {
                throw new ConflictException("Ya existe otro producto con el mismo nombre");
            }

            product.setName(dto.getName());
            product.setDescription(dto.getDescription());
            product.setBrand(findBrand(dto.getBrandId()));
            product.setVariants(buildVariants(dto, product));

            // Imágenes: solo agregar las nuevas, sin superar el límite de 6
            if (dto.getImages() != null && !dto.getImages().isEmpty()) {
                if (product.getImages() == null) {
                    product.setImages(new ArrayList<>());
                }

                int currentImages = product.getImages().size();
                int newImagesCount = dto.getImages().size();

                if (currentImages + newImagesCount > 6) {
                    throw new BadRequestException(
                            "No se pueden tener más de 6 imágenes por producto. " +
                                    "Actualmente tiene " + currentImages + " y estás agregando " + newImagesCount + ".");
                }

                product.getImages().addAll(buildImages(dto, product));
            }

            product.setCategories(buildCategories(dto));

            Product savedProduct = productRepository.save(product);

            sendStockAlertsIfNeeded(savedProduct);

            return Optional.of(mapToResponse(savedProduct));

        } catch (BadRequestException | ConflictException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar producto: " + e.getMessage(), e);
        }
    }

    /**
     * Lista productos con paginación.
     *
     * @param page número de página
     * @param size cantidad de registros por página
     * @return lista paginada de productos
     */
    @Transactional(readOnly = true)
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
        return productRepository.findByActiveTrue(pageable).getContent().stream()
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
     * Listar todos los productos creados en los últimos 7 días.
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
     * Listar los productos nuevos que además estén activos.
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

    // ─── Helpers privados ─────────────────────────────────────────────────────

    /**
     * Valida campos obligatorios y consistencia del ProductRequestDTO.
     *
     * @param dto DTO a validar
     */
    private void validateProductRequest(ProductRequestDTO dto) {

        if (dto == null ||
                dto.getName() == null || dto.getName().isBlank() ||
                dto.getVariants() == null || dto.getVariants().isEmpty()) {
            throw new BadRequestException("Nombre y variantes son obligatorios");
        }

        if (dto.getImages() != null && dto.getImages().size() > 6) {
            throw new BadRequestException("Máximo 6 imágenes por producto");
        }

        if (dto.getImages() != null &&
                dto.getImages().stream().distinct().count() != dto.getImages().size()) {
            throw new ConflictException("No se permiten URLs de imagen duplicadas");
        }

        dto.getVariants().forEach(v -> {
            if (v.getSku() == null || v.getSku().isBlank())
                throw new BadRequestException("El SKU es obligatorio en todas las variantes");
            if (v.getPrice() == null || v.getPrice().doubleValue() <= 0)
                throw new BadRequestException("El precio debe ser mayor que 0");
            if (v.getStock() == null || v.getStock() < 0)
                throw new BadRequestException("El stock no puede ser negativo");
            if (v.getMinStock() == null || v.getMinStock() < 0)
                throw new BadRequestException("El minStock no puede ser negativo");
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
                .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada con ID: " + brandId));
    }

    /**
     * Construye la lista de variantes del producto.
     *
     * @param dto     DTO de entrada
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
     *
     * Las imágenes se reciben como URLs (Cloudinary) desde el frontend.
     * Máximo permitido: 6 imágenes por producto.
     *
     * @param dto     DTO de entrada
     * @param product producto padre
     * @return lista de imágenes
     */
    private List<ProductImage> buildImages(ProductRequestDTO dto, Product product) {
        if (dto.getImages() == null || dto.getImages().isEmpty()) {
            return new ArrayList<>();
        }

        return dto.getImages().stream()
                .map(url -> {
                    if (url == null || url.isBlank())
                        throw new BadRequestException("Se recibió una URL de imagen vacía");
                    if (!url.startsWith("https://res.cloudinary.com/"))
                        throw new BadRequestException("La imagen no pertenece a Cloudinary");
                    return ProductImage.builder()
                            .url(url)
                            .product(product)
                            .build();
                })
                .collect(Collectors.toList());
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
            throw new ResourceNotFoundException("No se encontraron las categorías indicadas");
        }
        return categories;
    }

    /**
     * Envía alertas de stock bajo de forma segura.
     * Si el servicio de alertas falla, solo loguea el error y no rompe la transacción.
     *
     * @param product producto guardado
     */
    private void sendStockAlertsIfNeeded(Product product) {
        if (product.getVariants() == null || product.getVariants().isEmpty())
            return;

        product.getVariants().forEach(variant -> {
            if (variant.getStock() <= variant.getMinStock()) {
                try {
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
                                    .build());
                } catch (Exception e) {
                    // Las alertas no deben interrumpir el guardado del producto
                    log.warn("No se pudo enviar alerta de stock para SKU {}: {}",
                            variant.getSku(), e.getMessage());
                }
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

        List<ProductVariant> variantList = product.getVariants() == null
                ? new ArrayList<>()
                : product.getVariants();

        List<ProductResponseDTO.VariantDTO> variants = variantList.stream()
                .map(v -> ProductResponseDTO.VariantDTO.builder()
                        .sku(v.getSku())
                        .price(v.getPrice())
                        .stock(v.getStock())
                        .minStock(v.getMinStock())
                        .build())
                .collect(Collectors.toList());

        List<ProductResponseDTO.ImageDTO> images = product.getImages() != null
                ? product.getImages().stream()
                        .map(i -> ProductResponseDTO.ImageDTO.builder()
                                .url(i.getUrl())
                                .build())
                        .collect(Collectors.toList())
                : new ArrayList<>();

        List<String> categories = product.getCategories() != null
                ? product.getCategories().stream()
                        .map(Category::getName)
                        .collect(Collectors.toList())
                : new ArrayList<>();

        return ProductResponseDTO.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .description(product.getDescription())
                .brandName(product.getBrand() != null ? product.getBrand().getName() : null)
                .createdAt(product.getCreatedAt())
                .status(Boolean.TRUE.equals(product.getActive()) ? "ACTIVE" : "INACTIVE")
                .variants(variants)
                .images(images)
                .categories(categories)
                .build();
    }
}
