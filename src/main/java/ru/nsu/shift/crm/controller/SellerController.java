package ru.nsu.shift.crm.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ru.nsu.shift.crm.dto.SellerCreateRequest;
import ru.nsu.shift.crm.dto.SellerDto;
import ru.nsu.shift.crm.dto.SellerUpdateRequest;
import ru.nsu.shift.crm.service.SellerService;

@RestController
@RequestMapping("/api/sellers")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;

    /** GET /api/sellers --- list all active sellers */
    @GetMapping
    public ResponseEntity<List<SellerDto>> getAllSellers() {
        return ResponseEntity.ok(sellerService.getAllSellers());
    }

    /** GET /api/sellers/{id} --- get one seller by id */
    @GetMapping("/{id}")
    public ResponseEntity<SellerDto> getSellerById(@PathVariable Long id) {
        return ResponseEntity.ok(sellerService.getSellerById(id));
    }

    /** POST /api/sellers --- create a new seller */
    @PostMapping
    public ResponseEntity<SellerDto> createSeller(@Valid @RequestBody SellerCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sellerService.createSeller(request));
    }

    /** PATCH /api/sellers/{id} --- partial update of seller */
    @PatchMapping("/{id}")
    public ResponseEntity<SellerDto> updateSeller(
            @PathVariable Long id,
            @Valid @RequestBody SellerUpdateRequest request) {
        return ResponseEntity.ok(sellerService.updateSeller(id, request));
    }

    /** DELETE /api/sellers/{id} --- soft-delete a seller */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSeller(@PathVariable Long id) {
        sellerService.deleteSeller(id);
        return ResponseEntity.noContent().build();
    }
}
