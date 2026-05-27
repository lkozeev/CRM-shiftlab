package ru.nsu.shift.crm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.nsu.shift.crm.dto.TransactionCreateRequest;
import ru.nsu.shift.crm.entity.Seller;
import ru.nsu.shift.crm.entity.Transaction;
import ru.nsu.shift.crm.enums.PaymentType;
import ru.nsu.shift.crm.repository.SellerRepository;
import ru.nsu.shift.crm.repository.TransactionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private Seller seller;

	@TestConfiguration
    static class JacksonTestConfig {
        @Bean
        public ObjectMapper testObjectMapper() {
            return new ObjectMapper().findAndRegisterModules();
        }
    }

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        sellerRepository.deleteAll();

        seller = sellerRepository.save(Seller.builder()
			.name("Kozeeva Lada").contactInfo("lada.kozeeve@best.sister")
			.registrationDate(LocalDateTime.now()).deleted(false).build());
    }

    private Transaction createTransaction(BigDecimal amount, PaymentType type, LocalDateTime date) {
        return transactionRepository.save(Transaction.builder()
			.seller(seller).amount(amount).paymentType(type)
			.transactionDate(date).build());
    }

    @Test
    @DisplayName("GET /api/transactions")
    void getAllTransactions_valid() throws Exception {
        createTransaction(new BigDecimal("100.00"), PaymentType.CASH, LocalDateTime.now());
        createTransaction(new BigDecimal("200.00"), PaymentType.CARD, LocalDateTime.now());

        mockMvc.perform(get("/api/transactions"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));
    }
    @Test
    @DisplayName("GET /api/transactions/{id}")
    void getTransactionById_valid() throws Exception {
        Transaction t = createTransaction(new BigDecimal("350.00"), PaymentType.TRANSFER, LocalDateTime.now());

        mockMvc.perform(get("/api/transactions/" + t.getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.amount").value(350.00))
			.andExpect(jsonPath("$.paymentType").value("TRANSFER"));
    }

    @Test
    @DisplayName("GET /api/transactions/{id} ")
    void getTransactionById_missing() throws Exception {
        mockMvc.perform(get("/api/transactions/999999"))
			.andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/transactions")
    void createTransaction_valid() throws Exception {
        TransactionCreateRequest request = TransactionCreateRequest.builder()
			.sellerId(seller.getId())
			.amount(new BigDecimal("750.00"))
			.paymentType(PaymentType.CARD)
			.transactionDate(LocalDateTime.of(2024, 4, 10, 10, 0))
			.build();

        mockMvc.perform(post("/api/transactions")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").exists())
			.andExpect(jsonPath("$.sellerName").value("Kozeeva Lada"))
			.andExpect(jsonPath("$.amount").value(750.00));
    }

    @Test
    @DisplayName("POST /api/transactions unknown seller")
    void createTransaction_unknownSeller() throws Exception {
        TransactionCreateRequest request = TransactionCreateRequest.builder()
			.sellerId(999999L)
			.amount(new BigDecimal("100.00"))
			.paymentType(PaymentType.CASH)
			.build();

        mockMvc.perform(post("/api/transactions")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/transactions zero amount")
    void createTransaction_zeroAmount() throws Exception {
        TransactionCreateRequest request = TransactionCreateRequest.builder()
			.sellerId(seller.getId())
			.amount(BigDecimal.ZERO)
			.paymentType(PaymentType.CASH)
			.build();

        mockMvc.perform(post("/api/transactions")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/transactions/seller/{sellerId}")
    void getTransactionsBySeller_valid() throws Exception {
        createTransaction(new BigDecimal("100.00"), PaymentType.CASH, LocalDateTime.now());

        mockMvc.perform(get("/api/transactions/seller/" + seller.getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[*].sellerId", everyItem(is(seller.getId().intValue()))));
    }
}
