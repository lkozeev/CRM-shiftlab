package ru.nsu.shift.crm.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import ru.nsu.shift.crm.dto.BestPeriodDto;
import ru.nsu.shift.crm.dto.SellerDto;
import ru.nsu.shift.crm.entity.Seller;
import ru.nsu.shift.crm.entity.Transaction;
import ru.nsu.shift.crm.enums.PaymentType;
import ru.nsu.shift.crm.enums.Period;
import ru.nsu.shift.crm.exception.ResourceNotFoundException;
import ru.nsu.shift.crm.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private SellerService sellerService;

    @InjectMocks
    private AnalyticsService analyticsService;

    private Seller seller1;
    private Seller seller2;
    private SellerDto seller1Dto;

    @BeforeEach
    void setUp() {
        seller1 = Seller.builder().id(1L).name("Top Seller").contactInfo("top@shift.com")
            .registrationDate(LocalDateTime.now()).deleted(false).build();
        seller2 = Seller.builder().id(2L).name("Low Seller").contactInfo("low@shift.com")
            .registrationDate(LocalDateTime.now()).deleted(false).build();
        seller1Dto = SellerDto.builder().id(1L).name("Top Seller").contactInfo("top@shift.com")
            .registrationDate(LocalDateTime.now()).build();
    }

    @Test
    @DisplayName("getMostProductiveSeller DAY period")
    void getMostProductiveSeller_dayPeriod_returnsTopSeller() {
        when(transactionRepository.findTopSellersByTotalAmount(any(), any(), any())).thenReturn(List.of(seller1));

        SellerDto result = analyticsService.getMostProductiveSeller(Period.DAY, LocalDate.of(6767, 6, 7));

        assertThat(result.getName()).isEqualTo("Top Seller");
    }

    @Test
    @DisplayName("getMostProductiveSeller referenceDate null")
    void getMostProductiveSeller_nullReferenceDate_usesToday() {
        when(transactionRepository.findTopSellersByTotalAmount(any(), any(), any())).thenReturn(List.of(seller1));

        SellerDto result = analyticsService.getMostProductiveSeller(Period.MONTH, null);

        assertThat(result).isNotNull();
        verify(transactionRepository).findTopSellersByTotalAmount(any(), any(), eq(PageRequest.of(0, 1)));
    }

    @Test
    @DisplayName("getMostProductiveSeller throws when no transactions in period")
    void getMostProductiveSeller_noTransactions_throwsNotFoundException() {
        when(transactionRepository.findTopSellersByTotalAmount(any(), any(), any())).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> analyticsService.getMostProductiveSeller(Period.YEAR, LocalDate.of(6767, 6, 7)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("buildDateRange DAY")
    void buildDateRange_day_correctRange() {
        LocalDate date = LocalDate.of(2026, 6, 15);
        var range = Period.DAY.calculateRange(date);

        assertThat(range.start()).isEqualTo(LocalDateTime.of(2026, 6, 15, 0, 0, 0));
        assertThat(range.end().toLocalDate()).isEqualTo(date);
        assertThat(range.end().getHour()).isEqualTo(23);
    }

    @Test
    @DisplayName("buildDateRange MONTH")
    void buildDateRange_month_coversFullMonth() {
        LocalDate date = LocalDate.of(2024, 2, 14);
        var range = Period.MONTH.calculateRange(date);

        assertThat(range.start().toLocalDate()).isEqualTo(LocalDate.of(2024, 2, 1));
        assertThat(range.end().toLocalDate()).isEqualTo(LocalDate.of(2024, 2, 29));
    }

    @Test
    @DisplayName("buildDateRange QUARTER: Q1")
    void buildDateRange_quarter_q1_spansJanToMar() {
        LocalDate date = LocalDate.of(2024, 2, 1);
        var range = Period.QUARTER.calculateRange(date);

        assertThat(range.start().getMonthValue()).isEqualTo(1);
        assertThat(range.end().getMonthValue()).isEqualTo(3);
    }

    @Test
    @DisplayName("buildDateRange QUARTER: Q3")
    void buildDateRange_quarter_q3_spansJulToSep() {
        LocalDate date = LocalDate.of(2024, 8, 10);
        var range = Period.QUARTER.calculateRange(date);

        assertThat(range.start().getMonthValue()).isEqualTo(7);
        assertThat(range.end().getMonthValue()).isEqualTo(9);
    }

    @Test
    @DisplayName("buildDateRange YEAR")
    void buildDateRange_year_coversFullYear() {
        LocalDate date = LocalDate.of(2024, 7, 4);
        var range = Period.YEAR.calculateRange(date);

        assertThat(range.start().toLocalDate()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(range.end().toLocalDate()).isEqualTo(LocalDate.of(2024, 12, 31));
    }

    @Test
    @DisplayName("getSellersWithTotalBelow returns sellers")
    void getSellersWithTotalBelow_returnsMatchingSellers() {
        when(transactionRepository.findSellersWithTotalBelow(any(), any(), any())).thenReturn(List.of(seller2));
        // when(sellerMapper.toDtoList(List.of(seller2))).thenReturn(List.of(SellerDto.builder().id(2L).name("Low Seller").build()));

        List<SellerDto> result = analyticsService.getSellersWithTotalBelow(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31), new BigDecimal("1000"));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Low Seller");
    }

    @Test
    @DisplayName("getSellersWithTotalBelow throws IllegalArgumentException")
    void getSellersWithTotalBelow_invalidRange_throwsIllegalArgument() {
        assertThatThrownBy(() -> analyticsService.getSellersWithTotalBelow(
            LocalDate.of(2024, 3, 31), LocalDate.of(2024, 1, 1), new BigDecimal("1000")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("startDate");
    }

    @Test
    @DisplayName("getBestPeriodForSeller throws when seller has no transactions")
    void getBestPeriodForSeller_noTransactions_throwsNotFoundException() {
        when(sellerService.findSellerById(1L)).thenReturn(seller1);
        when(transactionRepository.findBySellerIdOrderByTransactionDateAsc(1L))
            .thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> analyticsService.getBestPeriodForSeller(1L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getBestPeriodForSeller returns the busiest window")
    void getBestPeriodForSeller_withTransactions_returnsBestPeriod() {
        // 3 transactions on 2024-03-01, 1 transaction on 2024-03-08
        Transaction t1 = makeTransaction(1L, seller1, LocalDateTime.of(2024, 3, 1, 10, 0));
        Transaction t2 = makeTransaction(2L, seller1, LocalDateTime.of(2024, 3, 1, 14, 0));
        Transaction t3 = makeTransaction(3L, seller1, LocalDateTime.of(2024, 3, 1, 18, 0));
        Transaction t4 = makeTransaction(4L, seller1, LocalDateTime.of(2024, 3, 8, 10, 0));

        when(sellerService.findSellerById(1L)).thenReturn(seller1);
        when(transactionRepository.findBySellerIdOrderByTransactionDateAsc(1L))
            .thenReturn(List.of(t1, t2, t3, t4));

        BestPeriodDto result = analyticsService.getBestPeriodForSeller(1L);

        assertThat(result.getTransactionCount()).isGreaterThanOrEqualTo(3);
        assertThat(result.getSellerId()).isEqualTo(1L);
    }

    private Transaction makeTransaction(long id, Seller s, LocalDateTime date) {
        return Transaction.builder()
            .id(id).seller(s).amount(BigDecimal.TEN)
            .paymentType(PaymentType.CASH).transactionDate(date)
            .build();
    }
}
