package com.api.Store.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class StoreCmsDTO {

    @Valid private AboutDTO about;
    @Valid private ContactDTO contact;
    @Valid private LocationsDTO locations;
    @Valid private ReturnsDTO returns;
    @Valid private FaqDTO faq;

    // ── About ─────────────────────────────────────────────────────────────────
    @Data
    public static class AboutDTO {
        @Size(max = 255) private String headline;
        private String story;
        private String mission;
        private String vision;
        @Size(max = 10)  private String founded;
        @Size(max = 50)  private String teamSize;
        private Boolean showTeam;
        private Boolean showTimeline;
    }

    // ── Contact ───────────────────────────────────────────────────────────────
    @Data
    public static class ContactDTO {
        @Size(max = 255) private String email;
        @Size(max = 50)  private String phone;
        @Size(max = 50)  private String whatsapp;
        @Size(max = 100) private String instagram;
        @Size(max = 100) private String tiktok;
        @Size(max = 255) private String hours;
        @Size(max = 255) private String formTitle;
        @Size(max = 255) private String formSubtitle;
        private Boolean showForm;
        private Boolean showSocials;
    }

    // ── Locations ─────────────────────────────────────────────────────────────
    @Data
    public static class LocationsDTO {
        private Boolean showMap;
        @Valid private List<LocationItemDTO> items;
    }

    @Data
    public static class LocationItemDTO {
        @Size(max = 255) private String name;
        private String address;
        @Size(max = 100) private String city;
        @Size(max = 50)  private String phone;
        @Size(max = 255) private String hours;
        private String mapUrl;
        private Boolean isPrimary;
        private Integer sortOrder;
    }

    // ── Returns ───────────────────────────────────────────────────────────────
    @Data
    public static class ReturnsDTO {
        @Size(max = 255) private String title;
        private String intro;
        private Short days;
        private String conditions;
        private String process;
        private String exceptions;
        @Size(max = 20)  private String refundMethod;
        private Boolean allowExchanges;
        private Boolean allowRefunds;
        private Boolean requireReceipt;
        @Size(max = 255) private String contactEmail;
    }

    // ── FAQ ───────────────────────────────────────────────────────────────────
    @Data
    public static class FaqDTO {
        @Size(max = 255) private String pageTitle;
        @Size(max = 255) private String pageSubtitle;
        private Boolean showSearch;
        @Valid private List<FaqItemDTO> items;
    }

    @Data
    public static class FaqItemDTO {
        private String question;
        private String answer;
        @Size(max = 50) private String category;
        private Integer sortOrder;
    }
}
