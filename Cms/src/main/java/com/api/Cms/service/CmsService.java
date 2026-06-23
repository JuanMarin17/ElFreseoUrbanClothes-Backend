package com.api.Cms.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.api.Cms.clients.PreferencesClient;
import com.api.Cms.clients.ProductClient;
import com.api.Cms.dto.CmsGenerateRequestDTO;
import com.api.Cms.dto.CmsPageResponseDTO;
import com.api.Cms.dto.ProductCardDTO;
import com.api.Cms.entity.CmsPage;
import com.api.Cms.exception.BadRequestException;
import com.api.Cms.exception.UnauthorizedException;
import com.api.Cms.repository.CmsPageRepository;
import com.common_request_context_starter.context.RequestContext;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CmsService {

    private final CmsPageRepository cmsPageRepository;
    private final GeminiService geminiService;
    private final PreferencesClient preferencesClient;
    private final ProductClient productClient;

    // ── Generar página personalizada con IA ───────────────────────────────────
    public CmsPageResponseDTO generatePage(CmsGenerateRequestDTO dto) {
        String userId = getUserIdFromHeader();
        String storeId = getStoreIdFromHeader();

        // Obtener historial y preferencias del usuario
        List<Map<String, Object>> behaviors = preferencesClient.getUserBehaviors(userId);
        List<Map<String, Object>> preferences = preferencesClient.getUserPreferences(userId);

        // Obtener productos activos de la tienda
        List<Map<String, Object>> products = productClient.getActiveProducts(storeId);

        // Construir prompt para Gemini
        String prompt = buildPrompt(userId, behaviors, preferences, products, dto.getQuery());

        // Llamar a Gemini
        String aiResponse = geminiService.generate(prompt);

        // Guardar la página generada
        CmsPage page = new CmsPage();
        page.setUserId(UUID.fromString(userId));
        page.setStoreId(UUID.fromString(storeId));
        page.setTitle("Recomendaciones para ti");
        page.setContent(aiResponse);

        CmsPage saved = cmsPageRepository.save(page);

        // Construir cards de productos recomendados
        List<ProductCardDTO> recommendations = buildProductCards(products, aiResponse);

        return toResponse(saved, recommendations);
    }

    // ── Obtener páginas del usuario ───────────────────────────────────────────
    public Page<CmsPageResponseDTO> getMyPages(Pageable pageable) {
        String userId = getUserIdFromHeader();
        return cmsPageRepository.findByUserIdOrderByCreatedAtDesc(UUID.fromString(userId), pageable)
                .map(p -> toResponse(p, List.of()));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private String buildPrompt(String userId, List<Map<String, Object>> behaviors,
            List<Map<String, Object>> preferences, List<Map<String, Object>> products,
            String userQuery) {

        StringBuilder sb = new StringBuilder();
        sb.append("Eres un asistente de recomendaciones de moda para una tienda de ropa urbana. ");
        sb.append("Basándote en el comportamiento e intereses del usuario, recomienda productos relevantes.\n\n");

        sb.append("COMPORTAMIENTO DEL USUARIO:\n");
        behaviors.forEach(b -> sb.append("- Evento: ").append(b.get("eventType"))
                .append(", Producto: ").append(b.get("productId")).append("\n"));

        sb.append("\nPREFERENCIAS DEL USUARIO:\n");
        preferences.forEach(p -> sb.append("- ").append(p.get("preferenceType"))
                .append(": ").append(p.get("preferenceValue")).append("\n"));

        sb.append("\nPRODUCTOS DISPONIBLES EN LA TIENDA:\n");
        products.stream().limit(20).forEach(p -> {
            sb.append("- ID: ").append(p.get("productId"))
                    .append(", Nombre: ").append(p.get("name"))
                    .append(", Categoría: ").append(p.get("categories"))
                    .append("\n");
        });

        if (userQuery != null && !userQuery.isBlank()) {
            sb.append("\nEL USUARIO PIDE: ").append(userQuery).append("\n");
        }

        sb.append("\nResponde con una lista de máximo 10 IDs de productos recomendados ");
        sb.append("separados por comas, y una breve descripción de por qué los recomiendas. ");
        sb.append("Formato: IDS: id1,id2,id3 | RAZON: descripción");

        return sb.toString();
    }

    private List<ProductCardDTO> buildProductCards(
            List<Map<String, Object>> products, String aiResponse) {

        try {
            if (!aiResponse.contains("IDS:"))
                return List.of();

            String idsSection = aiResponse.split("IDS:")[1].split("\\|")[0].trim();
            String[] recommendedIds = idsSection.split(",");

            return products.stream()
                    .filter(p -> {
                        String productId = String.valueOf(p.get("productId"));
                        for (String id : recommendedIds) {
                            if (productId.equals(id.trim()))
                                return true;
                        }
                        return false;
                    })
                    .map(p -> {
                        ProductCardDTO card = new ProductCardDTO();
                        card.setProductId(UUID.fromString(String.valueOf(p.get("productId"))));
                        card.setName(String.valueOf(p.get("name")));
                        card.setDescription(String.valueOf(p.get("description")));
                        card.setCategory(String.valueOf(p.get("categories")));

                        List<Map<String, Object>> images = (List<Map<String, Object>>) p.get("images");
                        if (images != null && !images.isEmpty()) {
                            card.setImageUrl(String.valueOf(images.get(0).get("url")));
                        }

                        List<Map<String, Object>> variants = (List<Map<String, Object>>) p.get("variants");
                        if (variants != null && !variants.isEmpty()) {
                            Object price = variants.get(0).get("price");
                            if (price != null) {
                                card.setPrice(new java.math.BigDecimal(String.valueOf(price)));
                            }
                        }

                        return card;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }

    private String getUserIdFromHeader() {
        String userIdHeader = RequestContext.getHeader("X-User-Id");
        if (userIdHeader == null || userIdHeader.isBlank())
            throw new UnauthorizedException("Usuario no autenticado");
        return userIdHeader;
    }

    private String getStoreIdFromHeader() {
        String storeIdHeader = RequestContext.getHeader("X-Store-Id");
        if (storeIdHeader == null || storeIdHeader.isBlank())
            throw new BadRequestException("No se encontró el X-Store-Id en el header");
        return storeIdHeader;
    }

    private CmsPageResponseDTO toResponse(CmsPage page, List<ProductCardDTO> recommendations) {
        CmsPageResponseDTO dto = new CmsPageResponseDTO();
        dto.setPageId(page.getPageId());
        dto.setStoreId(page.getStoreId());
        dto.setUserId(page.getUserId());
        dto.setTitle(page.getTitle());
        dto.setContent(page.getContent());
        dto.setRecommendations(recommendations);
        dto.setCreatedAt(page.getCreatedAt());
        return dto;
    }
}