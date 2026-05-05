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
import org.springframework.web.multipart.MultipartFile;

import com.api.product.dto.ProductRequestDTO;
import com.api.product.dto.ProductResponseDTO;
import com.api.product.entity.Brand;
import com.api.product.entity.Category;
import com.api.product.entity.Product;
import com.api.product.entity.ProductImage;
import com.api.product.entity.ProductVariant;
import com.api.product.repository.BrandRepository;
import com.api.product.repository.CategoryRepository;
import com.api.product.repository.ProductRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.RequiredArgsConstructor;

/**
 * Servicio para gestionar productos.
 * Incluye métodos para crear, actualizar, listar, buscar, activar, desactivar y paginar productos.
 * También sube las imágenes a Cloudinary automáticamente.
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final Cloudinary cloudinary;

    /**
     * Crea un nuevo producto en la base de datos.
     * Valida campos obligatorios, crea variantes, imágenes y categorías.
     * Las imágenes se suben a Cloudinary automáticamente.
     *
     * @param dto DTO con los datos del producto a crear
     * @return ProductResponseDTO con los datos del producto creado
     */
    public ProductResponseDTO createProduct(ProductRequestDTO dto) {
        try {
            // Validaciones básicas
            if (dto == null ||
                dto.getName() == null || dto.getName().isBlank() ||
                dto.getVariants() == null || dto.getVariants().isEmpty()) {

                throw new RuntimeException("Datos obligatorios faltantes");
            }

            // Verificar si existe producto con mismo nombre
            if (productRepository.existsByNameIgnoreCase(dto.getName())) {
                throw new RuntimeException("El producto ya existe");
            }

            // Buscar marca si existe
            Brand brand = null;
            if (dto.getBrandId() != null) {
                brand = brandRepository.findById(dto.getBrandId())
                        .orElseThrow(() -> new RuntimeException("Marca no encontrada"));
            }

            // Crear producto
            Product product = Product.builder()
                    .name(dto.getName())
                    .description(dto.getDescription())
                    .brand(brand)
                    .active(true) // al crearse, siempre activo
                    .build();

            // Crear variantes
            List<ProductVariant> variants = new ArrayList<>();
            dto.getVariants().forEach(v -> {
                if (v.getSku() == null || v.getSku().isBlank() ||
                    v.getPrice() == null || v.getPrice().doubleValue() <= 0 ||
                    v.getStock() == null || v.getStock() < 0) {
                    throw new RuntimeException("Datos inválidos en variantes");
                }

                ProductVariant variant = ProductVariant.builder()
                        .sku(v.getSku())
                        .price(v.getPrice())
                        .stock(v.getStock())
                        .product(product)
                        .build();
                variants.add(variant);
            });
            product.setVariants(variants);

            // Subir imágenes a Cloudinary y guardar URLs
            List<ProductImage> images = new ArrayList<>();
            if (dto.getImages() != null) {
                dto.getImages().forEach(img -> {
                    String url = uploadImageToCloudinary(img);
                    ProductImage image = ProductImage.builder()
                            .url(url)
                            .product(product)
                            .build();
                    images.add(image);
                });
            }
            product.setImages(images);

            // Asignar categorías
            if (dto.getCategoryIds() != null && !dto.getCategoryIds().isEmpty()) {
                List<Category> categories = categoryRepository.findAllById(dto.getCategoryIds());
                if (categories.isEmpty()) {
                    throw new RuntimeException("Categorías no encontradas");
                }
                product.setCategories(categories);
            }

            productRepository.save(product);
            return mapToResponse(product);

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
    public Optional<ProductResponseDTO> updateProduct(UUID id, ProductRequestDTO dto) {
        Optional<Product> optional = productRepository.findById(id);
        if (optional.isEmpty())
            return Optional.empty();

        Product product = optional.get();
        try {
            // Validaciones
            if (dto == null || dto.getName() == null || dto.getName().isBlank() ||
                    dto.getVariants() == null || dto.getVariants().isEmpty()) {
                throw new RuntimeException("Datos obligatorios faltantes");
            }

            if (!product.getName().equalsIgnoreCase(dto.getName()) &&
                    productRepository.existsByNameIgnoreCase(dto.getName())) {
                throw new RuntimeException("Ya existe otro producto con el mismo nombre");
            }

            product.setName(dto.getName());
            product.setDescription(dto.getDescription());

            // Marca
            if (dto.getBrandId() != null) {
                product.setBrand(
                        brandRepository.findById(dto.getBrandId())
                                .orElseThrow(() -> new RuntimeException("Marca no encontrada")));
            } else {
                product.setBrand(null);
            }

            // Variantes
            List<ProductVariant> variants = dto.getVariants().stream()
                    .map(v -> ProductVariant.builder()
                            .sku(v.getSku())
                            .price(v.getPrice())
                            .stock(v.getStock())
                            .product(product)
                            .build())
                    .collect(Collectors.toList());
            product.setVariants(variants);

            // Imágenes
            List<ProductImage> images = new ArrayList<>();
            if (dto.getImages() != null) {
                dto.getImages().forEach(img -> {
                    String url = uploadImageToCloudinary(img);
                    ProductImage image = ProductImage.builder()
                            .url(url)
                            .product(product)
                            .build();
                    images.add(image);
                });
            }
            product.setImages(images);

            // Categorías
            if (dto.getCategoryIds() != null && !dto.getCategoryIds().isEmpty()) {
                List<Category> categories = categoryRepository.findAllById(dto.getCategoryIds());
                product.setCategories(categories.isEmpty() ? new ArrayList<>() : categories);
            } else {
                product.setCategories(new ArrayList<>());
            }

            productRepository.save(product);
            return Optional.of(mapToResponse(product));

        } catch (RuntimeException e) {
            throw new RuntimeException("Error al actualizar producto: " + e.getMessage());
        }
    }

    /**
     * Lista productos con paginación.
     * @param page
     * @param size
     * @return
     */
    public List<ProductResponseDTO> listProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findAll(pageable);
        return productPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lista solo productos activos con paginación.
     * @param page
     * @param size
     * @return
     */
    public List<ProductResponseDTO> listActiveProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findByActiveTrue(pageable);
        return productPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Busca producto por ID.
     * @param id
     * @return
     */
    public Optional<ProductResponseDTO> getById(UUID id) {
        return productRepository.findById(id)
                .map(this::mapToResponse);
    }

    /**
     * Desactiva un producto.
     * @param id
     * @return
     */
    public Optional<ProductResponseDTO> inactiveProduct(UUID id) {
        Optional<Product> optional = productRepository.findById(id);
        if (optional.isEmpty()) return Optional.empty();

        Product product = optional.get();
        product.setActive(false);
        productRepository.save(product);
        return Optional.of(mapToResponse(product));
    }

    /**
     * Activa un producto.
     * @param id
     * @return
     */
    public Optional<ProductResponseDTO> activeProduct(UUID id) {
        Optional<Product> optional = productRepository.findById(id);
        if (optional.isEmpty()) return Optional.empty();

        Product product = optional.get();
        product.setActive(true);
        productRepository.save(product);
        return Optional.of(mapToResponse(product));
    }

    /**
     * Sube una imagen a Cloudinary y devuelve la URL pública.
     * @param file
     * @return
     */
    private String uploadImageToCloudinary(MultipartFile file) {
        try {
            var result = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("folder", "productos"));
            return result.get("secure_url").toString();
        } catch (Exception e) {
            throw new RuntimeException("Error al subir imagen: " + e.getMessage());
        }
    }

    /**
     * Convierte entidad Product a ProductResponseDTO.
     * @param product
     * @return
     */
    private ProductResponseDTO mapToResponse(Product product) {
        List<ProductResponseDTO.VariantDTO> variants = product.getVariants().stream()
                .map(v -> ProductResponseDTO.VariantDTO.builder()
                        .sku(v.getSku())
                        .price(v.getPrice())
                        .stock(v.getStock())
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
                .status(product.getActive() ? "ACTIVE" : "INACTIVE").variants(variants)
                .images(images)
                .categories(categories)
                .build();
    }

    /**
     * Listar todos los productos sin restricción
     * @return
     */
    public List<ProductResponseDTO> listAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    /**
     * Listar productos que unicamnete esten activos en el sistema
     * @return
     */
    
   public List<ProductResponseDTO> listAllActiveProducts() {
        return productRepository.findByActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * listar todos los productos que se hayan creados en un lapso de tiempo de 7 dias 
     * @return
     */
    public List<ProductResponseDTO> listNewProducts() {
        OffsetDateTime oneWeekAgo = OffsetDateTime.now().minusDays(7);

        return productRepository.findByCreatedAtAfter(oneWeekAgo).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Listar los productos nuevos pero que ademas su estado se encuentre activo en el momento
     * @return
     */
    public List<ProductResponseDTO> listNewActiveProducts() {
        OffsetDateTime oneWeekAgo = OffsetDateTime.now().minusDays(7);

        return productRepository.findByActiveTrueAndCreatedAtAfter(oneWeekAgo).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

}