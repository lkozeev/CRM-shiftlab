package ru.nsu.shift.crm.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.nsu.shift.crm.dto.SellerCreateRequest;
import ru.nsu.shift.crm.dto.SellerDto;
import ru.nsu.shift.crm.dto.SellerUpdateRequest;
import ru.nsu.shift.crm.entity.Seller;
import ru.nsu.shift.crm.exception.ResourceNotFoundException;
import ru.nsu.shift.crm.repository.SellerRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerService {
    private final SellerRepository sellerRepository;

    static SellerDto convertToDto(Seller seller) {
        return SellerDto.builder()
            .id(seller.getId())
            .name(seller.getName())
            .contactInfo(seller.getContactInfo())
            .registrationDate(seller.getRegistrationDate())
            .build();
    }

    Seller findSellerById(Long id) {
        return sellerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("seller", id));
    }
    /**
     * Returns the seller with the specified ID, or throw ResourceNotFoundException if not found.
     */
    @Transactional(readOnly = true)
    public SellerDto getSellerById(Long id) {
        return convertToDto(findSellerById(id));
    }

    /**
     * Returns a list of all sellers.
     */
    @Transactional(readOnly = true)
    public List<SellerDto> getAllSellers() {
        return sellerRepository.findAll().stream()
            .map(SellerService::convertToDto)
            .collect(Collectors.toList());
    }

    /**
     * Creates a new seller based on the provided SellerCreateRequest and returns the created SellerDto.
     */
    @Transactional
    public SellerDto createSeller(SellerCreateRequest request) {
        Seller seller = Seller.builder()
            .name(request.getName())
            .contactInfo(request.getContactInfo())
            .registrationDate(LocalDateTime.now())
            .build();
        Seller savedSeller = sellerRepository.save(seller);
        log.info("created new seller with id {}", savedSeller.getId());
        return convertToDto(savedSeller);
    }

    /**
     * Updates the seller with the specified ID using the provided SellerUpdateRequest and returns the updated SellerDto.
     * If the seller with the specified ID does not exist, throws ResourceNotFoundException.
     */
    @Transactional
    public SellerDto updateSeller(Long id, SellerUpdateRequest request) {
        Seller seller = findSellerById(id);
        
        if (request.getName() != null) {
            seller.setName(request.getName());
        }
        if (request.getContactInfo() != null) {
            seller.setContactInfo(request.getContactInfo());
        }

        Seller updatedSeller = sellerRepository.save(seller);
        log.info("updated seller with id {}", updatedSeller.getId());
        return convertToDto(updatedSeller);
    }

    /**
     * Deletes the seller with the specified ID. If the seller with the specified ID does not exist, throws ResourceNotFoundException.
     */
    @Transactional
    public void deleteSeller(Long id) {
        Seller seller = findSellerById(id);
        sellerRepository.delete(seller);
        log.info("deleted seller with id {}", id);
    }
}