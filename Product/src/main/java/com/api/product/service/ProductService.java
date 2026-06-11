package com.api.product.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.product.client.StoreClient;
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
import com.api.product.exception.UnauthorizedException;
import com.api.product.repository.BrandRepository;
import com.api.product.repository.CategoryRepository;
import com.api.product.repository.ProductRepository;
import com.common_request_context_starter.context.RequestContext;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final StockAlertService stockAlertService;
    private final StoreClient storeClient;

    @Transactional
    public ProductResponseDTO createProduct(ProductRequestDTO dto) {
        validateAdminOrOwner();
        UUID storeId = getStoreIdFromHeader();

        if (!storeClient.existsStore(storeId))
            throw new ResourceNotFoundException("La tienda no existe con id: " + storeId);

        validateProductRequest(dto);

        if (productRepository.existsByNameIgnoreCaseAndStoreId(dto.getName(), storeId))
            throw new ConflictException("Ya existe un producto con ese nombre en esta tienda");

        Brand brand = findBrand(dto.getBrandId());

        Product product = Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .brand(brand)
                .storeId(storeId)
                .active(true)
                .build();

        product.setVariants(buildVariants(dto, product));
        product.setImages(buildImages(dto, product));
        product.setCategories(buildCategories(dto));

        Product saved = productRepository.save(product);
        sendStockAlertsIfNeeded(saved);

        return mapToResponse(saved);
    }

    @Transactional
    public Optional<ProductResponseDTO> updateProduct(UUID id, ProductRequestDTO dto) {
        validateAdminOrOwner();
        UUID storeId = getStoreIdFromHeader();

        Optional<Product> optional = productRepository.findById(id);
        if (optional.isEmpty())
            return Optional.empty();

        Product product = optional.get();

        if (!product.getStoreId().equals(storeId))
            throw new UnauthorizedException("Este producto no pertenece a tu tienda");

        validateProductRequest(dto);

        if (!product.getName().equalsIgnoreCase(dto.getName()) &&
                productRepository.existsByNameIgnoreCaseAndStoreId(dto.getName(), storeId))
            throw new ConflictException("Ya existe otro producto con ese nombre en esta tienda");

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setBrand(findBrand(dto.getBrandId()));
        product.setVariants(buildVariants(dto, product));

        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            if (product.getImages() == null)
                product.setImages(new ArrayList<>());
            int current = product.getImages().size();
            int newCount = dto.getImages().size();
            if (current + newCount > 6)
                throw new BadRequestException("No se pueden tener más de 6 imágenes por producto. " +
                        "Actualmente tiene " + current + " y estás agregando " + newCount + ".");
            product.getImages().addAll(buildImages(dto, product));
        }

        product.setCategories(buildCategories(dto));

        Product saved = productRepository.save(product);
        sendStockAlertsIfNeeded(saved);

        return Optional.of(mapToResponse(saved));
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> listProducts(int page, int size) {
        UUID storeId = getStoreIdFromHeader();
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findByStoreId(storeId, pageable)
                .getContent().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> listActiveProducts(int page, int size) {
        UUID storeId = getStoreIdFromHeader();
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findByStoreIdAndActiveTrue(storeId, pageable)
                .getContent().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<ProductResponseDTO> getById(UUID id) {
        return productRepository.findById(id).map(this::mapToResponse);
    }

    @Transactional
    public Optional<ProductResponseDTO> inactiveProduct(UUID id) {
        validateAdminOrOwner();
        UUID storeId = getStoreIdFromHeader();

        Optional<Product> optional = productRepository.findById(id);
        if (optional.isEmpty())
            return Optional.empty();

        Product product = optional.get();
        if (!product.getStoreId().equals(storeId))
            throw new UnauthorizedException("Este producto no pertenece a tu tienda");

        product.setActive(false);
        return Optional.of(mapToResponse(productRepository.save(product)));
    }

    @Transactional
    public Optional<ProductResponseDTO> activeProduct(UUID id) {
        validateAdminOrOwner();
        UUID storeId = getStoreIdFromHeader();

        Optional<Product> optional = productRepository.findById(id);
        if (optional.isEmpty())
            return Optional.empty();

        Product product = optional.get();
        if (!product.getStoreId().equals(storeId))
            throw new UnauthorizedException("Este producto no pertenece a tu tienda");

        product.setActive(true);
        return Optional.of(mapToResponse(productRepository.save(product)));
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> listAllProducts() {
        UUID storeId = getStoreIdFromHeader();
        return productRepository.findByStoreId(storeId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> listAllActiveProducts() {
        return productRepository.findByActiveTrue()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> listNewProducts() {
        UUID storeId = getStoreIdFromHeader();
        OffsetDateTime oneWeekAgo = OffsetDateTime.now().minusDays(7);
        return productRepository.findByStoreIdAndCreatedAtAfter(storeId, oneWeekAgo)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> listNewActiveProducts() {
        UUID storeId = getStoreIdFromHeader();
        OffsetDateTime oneWeekAgo = OffsetDateTime.now().minusDays(7);
        return productRepository.findByStoreIdAndActiveTrueAndCreatedAtAfter(storeId, oneWeekAgo)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private UUID getStoreIdFromHeader() {
        String storeIdHeader = RequestContext.getHeader("X-Store-Id");
        if (storeIdHeader == null || storeIdHeader.isBlank())
            throw new BadRequestException("No se encontró el X-Store-Id en el header");
        try {
            return UUID.fromString(storeIdHeader);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Formato inválido del storeId");
        }
    }

    private UUID getUserIdFromHeader() {
        String userIdHeader = RequestContext.getHeader("x-user-id");
        if (userIdHeader == null || userIdHeader.isBlank())
            throw new BadRequestException("No se encontró el X-Store-Id en el header");
        try {
            return UUID.fromString(userIdHeader);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Formato inválido del storeId");
        }
    }

    private void validateAdminOrOwner() {
        UUID storeId = getStoreIdFromHeader();
        UUID userId = getUserIdFromHeader();
        String role = storeClient.userRole(userId, storeId);

        System.out.println(role);
        if ("ADMIN".equals(role) || "OWNER".equals(role))
            throw new UnauthorizedException("Solo el ADMIN u OWNER pueden realizar esta acción");
    }

    private void validateProductRequest(ProductRequestDTO dto) {
        if (dto == null || dto.getName() == null || dto.getName().isBlank() ||
                dto.getVariants() == null || dto.getVariants().isEmpty())
            throw new BadRequestException("Nombre y variantes son obligatorios");

        if (dto.getImages() != null && dto.getImages().size() > 6)
            throw new BadRequestException("Máximo 6 imágenes por producto");

        if (dto.getImages() != null &&
                dto.getImages().stream().distinct().count() != dto.getImages().size())
            throw new ConflictException("No se permiten URLs de imagen duplicadas");

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

    private Brand findBrand(UUID brandId) {
        if (brandId == null)
            return null;
        return brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada con ID: " + brandId));
    }

    private List<ProductVariant> buildVariants(ProductRequestDTO dto, Product product) {
        return dto.getVariants().stream()
                .map(v -> ProductVariant.builder()
                        .sku(v.getSku())
                        .price(v.getPrice())
                        .stock(v.getStock())
                        .minStock(v.getMinStock())
                        .size(v.getSize())
                        .color(v.getColor())
                        .product(product)
                        .build())
                .collect(Collectors.toList());
    }

    private List<ProductImage> buildImages(ProductRequestDTO dto, Product product) {
        if (dto.getImages() == null || dto.getImages().isEmpty())
            return new ArrayList<>();

        return dto.getImages().stream()
                .map(url -> {
                    if (url == null || url.isBlank())
                        throw new BadRequestException("Se recibió una URL de imagen vacía");
                    if (!url.startsWith("https://res.cloudinary.com/"))
                        throw new BadRequestException("La imagen no pertenece a Cloudinary");
                    return ProductImage.builder().url(url).product(product).build();
                })
                .collect(Collectors.toList());
    }

    private List<Category> buildCategories(ProductRequestDTO dto) {
        if (dto.getCategoryIds() == null || dto.getCategoryIds().isEmpty())
            return new ArrayList<>();
        List<Category> categories = categoryRepository.findAllById(dto.getCategoryIds());
        if (categories.isEmpty())
            throw new ResourceNotFoundException("No se encontraron las categorías indicadas");
        return categories;
    }

    private String buildVariantLabel(ProductVariant variant) {
        StringBuilder sb = new StringBuilder(" (SKU: ").append(variant.getSku());
        if (variant.getSize() != null && !variant.getSize().isBlank())
            sb.append(" | Talla: ").append(variant.getSize());
        if (variant.getColor() != null && !variant.getColor().isBlank())
            sb.append(" | Color: ").append(variant.getColor());
        sb.append(")");
        return sb.toString();
    }

    private void sendStockAlertsIfNeeded(Product product) {
        if (product.getVariants() == null || product.getVariants().isEmpty())
            return;

        product.getVariants().forEach(variant -> {
            if (variant.getStock() <= variant.getMinStock()) {
                try {
                    String label = buildVariantLabel(variant);
                    stockAlertService.sendAlert(StockAlertDTO.builder()
                            .productId(product.getProductId())
                            .productName(product.getName())
                            .variantId(variant.getVariantId())
                            .sku(variant.getSku())
                            .size(variant.getSize())
                            .color(variant.getColor())
                            .stock(variant.getStock())
                            .minStock(variant.getMinStock())
                            .message("⚠️ Stock bajo: " + product.getName() + label)
                            .timestamp(OffsetDateTime.now())
                            .build());
                } catch (Exception e) {
                    System.out.println("No se pudo enviar alerta de stock para SKU "
                            + variant.getSku() + ": " + e.getMessage());
                }
            }
        });
    }

    private ProductResponseDTO mapToResponse(Product product) {
        List<ProductVariant> variantList = product.getVariants() == null
                ? new ArrayList<>()
                : product.getVariants();

        List<ProductResponseDTO.VariantDTO> variants = variantList.stream()
                .map(v -> ProductResponseDTO.VariantDTO.builder()
                        .variantId(v.getVariantId())
                        .sku(v.getSku())
                        .price(v.getPrice())
                        .stock(v.getStock())
                        .minStock(v.getMinStock())
                        .size(v.getSize())
                        .color(v.getColor())
                        .build())
                .collect(Collectors.toList());

        List<ProductResponseDTO.ImageDTO> images = product.getImages() != null
                ? product.getImages().stream()
                        .map(i -> ProductResponseDTO.ImageDTO.builder().url(i.getUrl()).build())
                        .collect(Collectors.toList())
                : new ArrayList<>();

        List<String> categories = product.getCategories() != null
                ? product.getCategories().stream().map(Category::getName).collect(Collectors.toList())
                : new ArrayList<>();

        return ProductResponseDTO.builder()
                .productId(product.getProductId())
                .storeId(product.getStoreId())
                .name(product.getName())
                .description(product.getDescription())
                .brandName(product.getBrand() != null ? product.getBrand().getName() : null)
                .createdAt(product.getCreatedAt())
                .status(Boolean.TRUE.equals(product.getActive()) ? "ACTIVE" : "INACTIVE")
                .variants(variants).images(images).categories(categories)
                .build();
    }
}