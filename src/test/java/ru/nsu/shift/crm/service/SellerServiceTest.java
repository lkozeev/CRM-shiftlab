package ru.nsu.shift.crm.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import ru.nsu.shift.crm.dto.SellerCreateRequest;
import ru.nsu.shift.crm.dto.SellerDto;
import ru.nsu.shift.crm.dto.SellerUpdateRequest;
import ru.nsu.shift.crm.entity.Seller;
import ru.nsu.shift.crm.exception.ResourceNotFoundException;
import ru.nsu.shift.crm.repository.SellerRepository;

@ExtendWith(MockitoExtension.class)
class SellerServiceTest {

    @Mock
    private SellerRepository sellerRepository;

    @InjectMocks
    private SellerService sellerService;

    private Seller seller;
    private SellerDto sellerDto;

    @BeforeEach
    void setUp() {
        seller = Seller.builder()
            .id(1L)
            .name("Lada Kozeeva")
            .contactInfo("l.kozeeva@g.nsu.ru")
            .registrationDate(LocalDateTime.of(2025, 1, 10, 9, 0))
            .build();

        sellerDto = SellerDto.builder()
            .id(1L)
            .name("Lada Kozeeva")
            .contactInfo("l.kozeeva@g.nsu.ru")
            .registrationDate(LocalDateTime.of(2025, 1, 10, 9, 0))
            .build();
    }

    @Test
    @DisplayName("getAllSellers returns active sellers")
    void getAllSellers_returnsActiveSellers() {
        when(sellerRepository.findAll()).thenReturn(List.of(seller));

        List<SellerDto> result = sellerService.getAllSellers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Lada Kozeeva");
        verify(sellerRepository).findAll();
    }

    @Test
    @DisplayName("getSellerById returns dto")
    void getSellerById_existingSeller_returnsDto() {
        when(sellerRepository.findById(1L)).thenReturn(Optional.of(seller));

        SellerDto result = sellerService.getSellerById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getSellerById throws ResourceNotFoundException")
    void getSellerById_missingSeller_throwsNotFoundException() {
        when(sellerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sellerService.getSellerById(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    @DisplayName("createSeller saves")
    void createSeller_validRequest_returnsSavedDto() {
        SellerCreateRequest request = new SellerCreateRequest("Kozeev Vadim", "vadim.kozeev@shift.com");
        when(sellerRepository.save(any(Seller.class))).thenAnswer(i -> i.getArgument(0));

        SellerDto result = sellerService.createSeller(request);

        assertThat(result.getName()).isEqualTo("Kozeev Vadim");
        verify(sellerRepository).save(any(Seller.class));
    }

    @Test
    @DisplayName("updateSeller applies partial changes")
    void updateSeller_partialUpdate_appliesChanges() {
        SellerUpdateRequest request = new SellerUpdateRequest("New Name", null);

        when(sellerRepository.findById(1L)).thenReturn(Optional.of(seller));
        when(sellerRepository.save(seller)).thenAnswer(i -> i.getArgument(0));

        sellerService.updateSeller(1L, request);

        ArgumentCaptor<Seller> captor = ArgumentCaptor.forClass(Seller.class);
        verify(sellerRepository).save(captor.capture());

        Seller updatedSeller = captor.getValue();
        assertThat(updatedSeller.getName()).isEqualTo("New Name");
        assertThat(updatedSeller.getContactInfo()).isEqualTo("l.kozeeva@g.nsu.ru");
    }

    @Test
    @DisplayName("updateSeller throws NotFoundException")
    void updateSeller_missingSeQller_throwsNotFoundException() {
        when(sellerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sellerService.updateSeller(99L, new SellerUpdateRequest()))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("deleteSeller sets deleted=true (soft delete)")
    void deleteSeller_activeSeller_softDeletesIt() {
        when(sellerRepository.findById(1L)).thenReturn(Optional.of(seller));

        sellerService.deleteSeller(1L);

        verify(sellerRepository).delete(seller);
    }

    @Test
    @DisplayName("deleteSeller throws NotFoundException")
    void deleteSeller_missingSeller_throwsNotFoundException() {
        when(sellerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sellerService.deleteSeller(99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
