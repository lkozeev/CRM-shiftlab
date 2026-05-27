package ru.nsu.shift.crm.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import ru.nsu.shift.crm.dto.TransactionCreateRequest;
import ru.nsu.shift.crm.dto.TransactionDto;
import ru.nsu.shift.crm.entity.Seller;
import ru.nsu.shift.crm.entity.Transaction;
import ru.nsu.shift.crm.enums.PaymentType;
import ru.nsu.shift.crm.exception.ResourceNotFoundException;
import ru.nsu.shift.crm.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private SellerService sellerService;

    @InjectMocks
    private TransactionService transactionService;

    private Seller seller;
    private Transaction transaction;
    private TransactionDto transactionDto;

    @BeforeEach
    void setUp() {
        seller = Seller.builder()
                .id(1L).name("Lada Kozeeva").contactInfo("l.kozeeva@g.nsu.ru")
                .registrationDate(LocalDateTime.now()).deleted(false).build();

        transaction = Transaction.builder()
                .id(10L).seller(seller)
                .amount(new BigDecimal("500.00"))
                .paymentType(PaymentType.CARD)
                .transactionDate(LocalDateTime.of(2024, 3, 15, 12, 0))
                .build();

        transactionDto = TransactionDto.builder()
                .id(10L).sellerId(1L).sellerName("Lada Kozeeva")
                .amount(new BigDecimal("500.00")).paymentType(PaymentType.CARD)
                .transactionDate(LocalDateTime.of(2024, 3, 15, 12, 0))
                .build();
    }

    @Test
    @DisplayName("getAllTransactions returns all transactions")
    void getAllTransactions_returnsMappedList() {
        when(transactionRepository.findAll()).thenReturn(List.of(transaction));

        List<TransactionDto> result = transactionService.getAllTransactions();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAmount()).isEqualByComparingTo("500.00");
    }

    @Test
    @DisplayName("getTransactionById returns dto for existing transaction")
    void getTransactionById_existing_returnsDto() {
        when(transactionRepository.findById(10L)).thenReturn(Optional.of(transaction));

        TransactionDto result = transactionService.getTransactionById(10L);

        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("getTransactionById throws for missing transaction")
    void getTransactionById_missing_throwsNotFoundException() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getTransactionById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getTransactionsBySeller returns seller transactions")
    void getTransactionsBySeller_existingSeller_returnsTransactions() {
        when(transactionRepository.findBySellerId(1L)).thenReturn(List.of(transaction));

        List<TransactionDto> result = transactionService.getTransactionsBySeller(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("createTransaction uses current time when transactionDate is null")
    void createTransaction_nullDate_usesCurrentTime() {
        TransactionCreateRequest request = TransactionCreateRequest.builder()
                .sellerId(1L)
                .amount(new BigDecimal("300.00"))
                .paymentType(PaymentType.CASH)
                .transactionDate(null)
                .build();

        when(sellerService.findSellerById(1L)).thenReturn(seller);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        TransactionDto result = transactionService.createTransaction(request);

        assertThat(result).isNotNull();
        verify(transactionRepository).save(argThat(t -> t.getTransactionDate() != null));
    }

    @Test
    @DisplayName("createTransaction preserves explicitly provided transactionDate")
    void createTransaction_withDate_preservesDate() {
        LocalDateTime specificDate = LocalDateTime.of(2024, 5, 20, 10, 30);
        TransactionCreateRequest request = TransactionCreateRequest.builder()
                .sellerId(1L)
                .amount(new BigDecimal("150.00"))
                .paymentType(PaymentType.TRANSFER)
                .transactionDate(specificDate)
                .build();

        when(sellerService.findSellerById(1L)).thenReturn(seller);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        transactionService.createTransaction(request);

        verify(transactionRepository).save(argThat(t -> specificDate.equals(t.getTransactionDate())));
    }
}
