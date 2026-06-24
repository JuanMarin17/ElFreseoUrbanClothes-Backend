package com.api.Store.dto.settings;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * DTO principal que recibe el payload completo del wizard de configuración.
 *
 * Ejemplo Postman (POST /api/stores/{storeId}/settings):
 * {
 * "completedStep": 7,
 * "plan": { "id": "basico", "name": "BÁSICO", "price": "$19", "features":
 * ["..."] },
 * "basic": { "name": "urbana", "description": "...", "logoPreview": "blob:..."
 * },
 * "components": {
 * "banner": { "title": "...", "font": "...", "size": "77", "color": "#ff0f0f",
 * "bg": "#c10b0b", "images": [] },
 * "header": { "logo": "...", "items": ["HOME","SHOP"], "font": "Inter", "size":
 * 16, "color": "#fff", "bg": "#000" },
 * "footer": { "text": "...", "font": "Montserrat", "size": 14, "color":
 * "#888888", "bg": "#080808" }
 * },
 * "layout": { "id": "clasico", "title": "CLÁSICO ECOMMERCE", "description":
 * "..." },
 * "legal": { "legalName": "...", "idNumber": "...", "documentName": "..." },
 * "payment": { "paymentMethod": "mercadopago", "shipping": "ambos" },
 * "preview": { ... },
 * "styles": { ... },
 * "store": { "name": "urbana", "subdomain": "...", "accepted": true }
 * }
 */
@Data
public class StoreSettingsRequestDTO {

    @NotNull(message = "completedStep es obligatorio")
    private Integer completedStep;

    /** URL o preview del logo (se extrae para guardarlo como campo dedicado) */
    private String logoUrl;

    @Valid
    private PlanSettingsDTO plan;

    @Valid
    private BasicSettingsDTO basic;

    @Valid
    private ComponentsSettingsDTO components;

    @Valid
    private LayoutSettingsDTO layout;

    @Valid
    private LegalSettingsDTO legal;

    @Valid
    private PaymentSettingsDTO payment;

    /** Preview: se almacena tal cual como JSONB */
    private Map<String, Object> preview;

    /** Styles: se almacena tal cual como JSONB */
    private Map<String, Object> styles;

    /** Modo mantenimiento: { enabled, message } */
    private Map<String, Object> maintenance;

    @Valid
    private StoreInfoSettingsDTO store;

    // ─────────────────────────────────────────────────────────────────────────
    // DTOs anidados
    // ─────────────────────────────────────────────────────────────────────────

    @Data
    public static class PlanSettingsDTO {
        private String id;
        private String name;
        private String price;
        private List<String> features;
    }

    @Data
    public static class BasicSettingsDTO {
        @NotNull(message = "El nombre básico de la tienda es obligatorio")
        private String name;
        private String description;
        /**
         * Preview de logo en base64 o blob URL (solo para referencia, no se persiste
         * tal cual)
         */
        private String logoPreview;
    }

    @Data
    public static class ComponentsSettingsDTO {
        @Valid
        private BannerSettingsDTO banner;
        @Valid
        private HeaderSettingsDTO header;
        @Valid
        private FooterSettingsDTO footer;
    }

    @Data
    public static class BannerSettingsDTO {
        private String title;
        private String font;
        private String size;
        private String color;
        private String bg;
        /** Lista de imágenes del banner: [{ id, width, radius }] */
        private String image;
    }

    @Data
    public static class HeaderSettingsDTO {
        private String logo;
        private List<String> items;
        private String font;
        private Integer size;
        private String color;
        private String bg;
    }

    @Data
    public static class FooterSettingsDTO {
        private String text;
        private String font;
        private String size; // Unificado a String
        private String color;
        private String bg;
    }

    @Data
    public static class LayoutSettingsDTO {
        private String id;
        private String title;
        private String description;
        private String img;
    }

    @Data
    public static class LegalSettingsDTO {
        private String legalName;
        private String idNumber;
        private String documentName;
    }

    @Data
    public static class PaymentSettingsDTO {
        private String paymentMethod;
        private String shipping;
    }

    @Data
    public static class StoreInfoSettingsDTO {
        private String name;
        private String subdomain;
        private Boolean accepted;
    }
}
