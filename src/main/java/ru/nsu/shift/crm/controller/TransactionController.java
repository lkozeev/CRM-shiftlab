package ru.nsu.shift.crm.controller;

import ru.nsu.shift.crm.dto.TransactionCreateRequest;
import ru.nsu.shift.crm.dto.TransactionDto;
import ru.nsu.shift.crm.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /** GET /api/transactions --- list all transactions */
    @GetMapping
    public ResponseEntity<List<TransactionDto>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    /** GET /api/transactions/{id} --- get one transaction by id */
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDto> getTransactionById(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }

    /** POST /api/transactions --- create a new transaction */
    @PostMapping
    public ResponseEntity<TransactionDto> createTransaction(
            @Valid @RequestBody TransactionCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.createTransaction(request));
    }

    /** GET /api/transactions/seller/{sellerId} --- get all transactions for a seller */
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<TransactionDto>> getTransactionsBySeller(@PathVariable Long sellerId) {
        return ResponseEntity.ok(transactionService.getTransactionsBySeller(sellerId));
    }
}
