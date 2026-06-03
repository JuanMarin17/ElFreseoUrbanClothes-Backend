package com.api.Store.service;

import com.api.Store.dto.StoreCmsDTO;
import com.api.Store.entity.*;
import com.api.Store.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreCmsService {

    private final StoreCmsAboutRepository aboutRepo;
    private final StoreCmsContactRepository contactRepo;
    private final StoreCmsLocationsConfigRepository locationsConfigRepo;
    private final StoreCmsLocationRepository locationRepo;
    private final StoreCmsReturnsRepository returnsRepo;
    private final StoreCmsFaqConfigRepository faqConfigRepo;
    private final StoreCmsFaqItemRepository faqItemRepo;

    public void saveCms(UUID storeId, StoreCmsDTO cms) {
        if (cms == null) return;
        saveAbout(storeId, cms.getAbout());
        saveContact(storeId, cms.getContact());
        saveLocations(storeId, cms.getLocations());
        saveReturns(storeId, cms.getReturns());
        saveFaq(storeId, cms.getFaq());
    }

    private void saveAbout(UUID storeId, StoreCmsDTO.AboutDTO dto) {
        if (dto == null) return;
        aboutRepo.save(StoreCmsAbout.builder()
                .storeId(storeId)
                .headline(dto.getHeadline())
                .story(dto.getStory())
                .mission(dto.getMission())
                .vision(dto.getVision())
                .founded(dto.getFounded())
                .teamSize(dto.getTeamSize())
                .showTeam(dto.getShowTeam())
                .showTimeline(dto.getShowTimeline())
                .updatedAt(OffsetDateTime.now())
                .build());
    }

    private void saveContact(UUID storeId, StoreCmsDTO.ContactDTO dto) {
        if (dto == null) return;
        contactRepo.save(StoreCmsContact.builder()
                .storeId(storeId)
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .whatsapp(dto.getWhatsapp())
                .instagram(dto.getInstagram())
                .tiktok(dto.getTiktok())
                .hours(dto.getHours())
                .formTitle(dto.getFormTitle())
                .formSubtitle(dto.getFormSubtitle())
                .showForm(dto.getShowForm())
                .showSocials(dto.getShowSocials())
                .updatedAt(OffsetDateTime.now())
                .build());
    }

    private void saveLocations(UUID storeId, StoreCmsDTO.LocationsDTO dto) {
        if (dto == null) return;

        locationsConfigRepo.save(StoreCmsLocationsConfig.builder()
                .storeId(storeId)
                .showMap(dto.getShowMap())
                .updatedAt(OffsetDateTime.now())
                .build());

        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            locationRepo.deleteByStoreId(storeId);
            List<StoreCmsLocation> locations = dto.getItems().stream()
                    .map(item -> StoreCmsLocation.builder()
                            .storeId(storeId)
                            .name(item.getName())
                            .address(item.getAddress())
                            .city(item.getCity())
                            .phone(item.getPhone())
                            .hours(item.getHours())
                            .mapUrl(item.getMapUrl())
                            .isPrimary(item.getIsPrimary())
                            .sortOrder(item.getSortOrder() != null ? item.getSortOrder() : 0)
                            .build())
                    .collect(Collectors.toList());
            locationRepo.saveAll(locations);
        }
    }

    private void saveReturns(UUID storeId, StoreCmsDTO.ReturnsDTO dto) {
        if (dto == null) return;
        returnsRepo.save(StoreCmsReturns.builder()
                .storeId(storeId)
                .title(dto.getTitle())
                .intro(dto.getIntro())
                .days(dto.getDays())
                .conditions(dto.getConditions())
                .process(dto.getProcess())
                .exceptions(dto.getExceptions())
                .refundMethod(dto.getRefundMethod())
                .allowExchanges(dto.getAllowExchanges())
                .allowRefunds(dto.getAllowRefunds())
                .requireReceipt(dto.getRequireReceipt())
                .contactEmail(dto.getContactEmail())
                .updatedAt(OffsetDateTime.now())
                .build());
    }

    private void saveFaq(UUID storeId, StoreCmsDTO.FaqDTO dto) {
        if (dto == null) return;

        faqConfigRepo.save(StoreCmsFaqConfig.builder()
                .storeId(storeId)
                .pageTitle(dto.getPageTitle())
                .pageSubtitle(dto.getPageSubtitle())
                .showSearch(dto.getShowSearch())
                .updatedAt(OffsetDateTime.now())
                .build());

        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            faqItemRepo.deleteByStoreId(storeId);
            List<StoreCmsFaqItem> items = dto.getItems().stream()
                    .map(item -> StoreCmsFaqItem.builder()
                            .storeId(storeId)
                            .question(item.getQuestion())
                            .answer(item.getAnswer())
                            .category(item.getCategory())
                            .sortOrder(item.getSortOrder() != null ? item.getSortOrder() : 0)
                            .build())
                    .collect(Collectors.toList());
            faqItemRepo.saveAll(items);
        }
    }
}
