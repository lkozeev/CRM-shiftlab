package ru.nsu.shift.crm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.nsu.shift.crm.dto.SellerCreateRequest;
import ru.nsu.shift.crm.dto.SellerUpdateRequest;
import ru.nsu.shift.crm.entity.Seller;
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

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SellerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        sellerRepository.deleteAll();
    }

    private Seller createSeller(String name, String contact) {
        return sellerRepository.save(Seller.builder()
                .name(name).contactInfo(contact)
                .registrationDate(LocalDateTime.now())
                .deleted(false).build());
    }

    @Test
    @DisplayName("GET /api/sellers")
    void getAllSellers_valid() throws Exception {
        createSeller("Lada", "lada@gmail.com");
        createSeller("Vadim", "vadim@gmail.com");

        mockMvc.perform(get("/api/sellers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    @DisplayName("GET /api/sellers deleted sellers")
    void getAllSellers_deleted() throws Exception {
        createSeller("lada", "lada@gmail.com");
        Seller deleted = createSeller("vadim", "vadim@gmail.com");
        deleted.setDeleted(true);
        sellerRepository.save(deleted);

        mockMvc.perform(get("/api/sellers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].name", not(hasItem("vadim"))));
    }

    @Test
    @DisplayName("GET /api/sellers/{id}")
    void getSellerById_valid() throws Exception {
        Seller s = createSeller("lera", "lera@gmail.com");

        mockMvc.perform(get("/api/sellers/" + s.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("lera"));
    }

    @Test
    @DisplayName("GET /api/sellers/{id} missing seller")
    void getSellerById_missingSeller() throws Exception {
        mockMvc.perform(get("/api/sellers/999999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }
    @Test
    @DisplayName("POST /api/sellers created seller")
    void createSeller_CreatedSeller() throws Exception {
        SellerCreateRequest request = new SellerCreateRequest("mira", "mira@gmail.com");

        mockMvc.perform(post("/api/sellers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.name").value("mira"))
            .andExpect(jsonPath("$.registrationDate").exists());
    }

    @Test
    @DisplayName("POST /api/sellers blank name")
    void createSeller_blankName() throws Exception {
        SellerCreateRequest request = new SellerCreateRequest("", "noname@67.67.67.67");

        mockMvc.perform(post("/api/sellers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/sellers/{id} updates seller name")
    void updateSeller_valid() throws Exception {
        Seller s = createSeller("lada", "lada@gmail.com");
        SellerUpdateRequest request = new SellerUpdateRequest("leon", null);

        mockMvc.perform(patch("/api/sellers/" + s.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("leon"))
            .andExpect(jsonPath("$.contactInfo").value("lada@gmail.com"));
    }

    @Test
    @DisplayName("DELETE /api/sellers/{id} double delete")
    void deleteSeller_doubleDelete() throws Exception {
        Seller s = createSeller("lada", "lada@gmail.com");

        mockMvc.perform(delete("/api/sellers/" + s.getId()))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/sellers/" + s.getId()))
            .andExpect(status().isNotFound());
    }
}
