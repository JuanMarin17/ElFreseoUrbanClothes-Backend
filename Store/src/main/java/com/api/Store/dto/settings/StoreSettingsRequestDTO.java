package com.api.Store.dto.settings;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

@Data
public class StoreSettingsRequestDTO {

    private String logoUrl;

    private String bannerUrl;

    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "El color primario debe ser un HEX válido (ej: #FF5733)")
    private String primaryColor;

    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "El color secundario debe ser un HEX válido (ej: #33A1FF)")
    private String secondaryColor;

    @Size(max = 50, message = "El nombre de la fuente no puede superar 50 caracteres")
    private String font;

    @Size(max = 30, message = "El tema no puede superar 30 caracteres")
    private String theme;

    private Map<String, Object> layout;

    @Size(min = 3, max = 3, message = "La moneda debe ser un código de 3 letras (ej: COP, USD)")
    private String currency;

    @Size(min = 2, max = 5, message = "El idioma debe ser un código válido (ej: es, en)")
    private String language;
}
