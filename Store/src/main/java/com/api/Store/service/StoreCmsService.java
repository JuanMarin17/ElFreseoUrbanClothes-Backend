package com.api.Store.service;

import com.api.Store.dto.StoreCmsDTO;
import com.api.Store.entity.*;
import com.api.Store.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public StoreCmsDTO getCms(UUID storeId) {
        StoreCmsDTO dto = new StoreCmsDTO();

        aboutRepo.findById(storeId).ifPresent(a -> {
            StoreCmsDTO.AboutDTO about = new StoreCmsDTO.AboutDTO();
            about.setHeadline(a.getHeadline());
            about.setStory(a.getStory());
            about.setMission(a.getMission());
            about.setVision(a.getVision());
            about.setFounded(a.getFounded());
            about.setTeamSize(a.getTeamSize());
            about.setShowTeam(a.getShowTeam());
            about.setShowTimeline(a.getShowTimeline());
            dto.setAbout(about);
        });

        contactRepo.findById(storeId).ifPresent(c -> {
            StoreCmsDTO.ContactDTO contact = new StoreCmsDTO.ContactDTO();
            contact.setEmail(c.getEmail());
            contact.setPhone(c.getPhone());
            contact.setWhatsapp(c.getWhatsapp());
            contact.setInstagram(c.getInstagram());
            contact.setTiktok(c.getTiktok());
            contact.setHours(c.getHours());
            contact.setFormTitle(c.getFormTitle());
            contact.setFormSubtitle(c.getFormSubtitle());
            contact.setShowForm(c.getShowForm());
            contact.setShowSocials(c.getShowSocials());
            dto.setContact(contact);
        });

        StoreCmsDTO.LocationsDTO locations = new StoreCmsDTO.LocationsDTO();
        locationsConfigRepo.findById(storeId).ifPresent(lc -> locations.setShowMap(lc.getShowMap()));
        List<StoreCmsDTO.LocationItemDTO> locationItems = locationRepo
                .findByStoreIdOrderBySortOrderAsc(storeId).stream()
                .map(l -> {
                    StoreCmsDTO.LocationItemDTO item = new StoreCmsDTO.LocationItemDTO();
                    item.setName(l.getName());
                    item.setAddress(l.getAddress());
                    item.setCity(l.getCity());
                    item.setPhone(l.getPhone());
                    item.setHours(l.getHours());
                    item.setMapUrl(l.getMapUrl());
                    item.setIsPrimary(l.getIsPrimary());
                    item.setSortOrder(l.getSortOrder());
                    return item;
                }).collect(Collectors.toList());
        locations.setItems(locationItems);
        dto.setLocations(locations);

        returnsRepo.findById(storeId).ifPresent(r -> {
            StoreCmsDTO.ReturnsDTO returns = new StoreCmsDTO.ReturnsDTO();
            returns.setTitle(r.getTitle());
            returns.setIntro(r.getIntro());
            returns.setDays(r.getDays());
            returns.setConditions(r.getConditions());
            returns.setProcess(r.getProcess());
            returns.setExceptions(r.getExceptions());
            returns.setRefundMethod(r.getRefundMethod());
            returns.setAllowExchanges(r.getAllowExchanges());
            returns.setAllowRefunds(r.getAllowRefunds());
            returns.setRequireReceipt(r.getRequireReceipt());
            returns.setContactEmail(r.getContactEmail());
            dto.setReturns(returns);
        });

        StoreCmsDTO.FaqDTO faq = new StoreCmsDTO.FaqDTO();
        faqConfigRepo.findById(storeId).ifPresent(fc -> {
            faq.setPageTitle(fc.getPageTitle());
            faq.setPageSubtitle(fc.getPageSubtitle());
            faq.setShowSearch(fc.getShowSearch());
        });
        List<StoreCmsDTO.FaqItemDTO> faqItems = faqItemRepo
                .findByStoreIdOrderBySortOrderAsc(storeId).stream()
                .map(fi -> {
                    StoreCmsDTO.FaqItemDTO item = new StoreCmsDTO.FaqItemDTO();
                    item.setQuestion(fi.getQuestion());
                    item.setAnswer(fi.getAnswer());
                    item.setCategory(fi.getCategory());
                    item.setSortOrder(fi.getSortOrder());
                    return item;
                }).collect(Collectors.toList());
        faq.setItems(faqItems);
        dto.setFaq(faq);

        return dto;
    }

    @Transactional
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
