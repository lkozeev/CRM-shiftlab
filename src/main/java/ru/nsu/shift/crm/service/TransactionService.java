package ru.nsu.shift.crm.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.nsu.shift.crm.dto.TransactionCreateRequest;
import ru.nsu.shift.crm.dto.TransactionDto;
import ru.nsu.shift.crm.entity.Seller;
import ru.nsu.shift.crm.entity.Transaction;
import ru.nsu.shift.crm.exception.ResourceNotFoundException;
import ru.nsu.shift.crm.repository.TransactionRepository;


@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final SellerService sellerService;

    static TransactionDto convertToDto(Transaction transaction) {
        return TransactionDto.builder()
            .id(transaction.getId())
            .amount(transaction.getAmount())
            .sellerId(transaction.getSeller().getId())
            .sellerName(transaction.getSeller().getName())
            .paymentType(transaction.getPaymentType())
            .transactionDate(transaction.getTransactionDate())
            .build();
    }

    /**
     * Returns the transaction with the specified ID, or throw ResourceNotFoundException if not found.
     */
    @Transactional(readOnly = true)
    public TransactionDto getTransactionById(Long id) {
        return transactionRepository.findById(id)
            .map(TransactionService::convertToDto)
            .orElseThrow(() -> new ResourceNotFoundException("transaction", id));
    }

    /**
     * Returns all transactions across all sellers.
     */
    @Transactional(readOnly = true)
    public List<TransactionDto> getAllTransactions() {
        return transactionRepository.findAll().stream()
            .map(TransactionService::convertToDto)
            .toList();
    }
    /**
     * Returns all transactions for the specified seller.
     */
    @Transactional(readOnly = true)
    public List<TransactionDto> getTransactionsBySeller(Long sellerId) {
        return transactionRepository.findBySellerId(sellerId).stream()
            .map(TransactionService::convertToDto)
            .toList();
    } 
    
    /**
     * Creates a new transaction based on the provided TransactionCreateRequest and returns the created TransactionDto.
     * If the seller with the specified ID does not exist, throws ResourceNotFoundException.
     */
    @Transactional
    public TransactionDto createTransaction(TransactionCreateRequest request) {
        Seller seller = sellerService.findSellerById(request.getSellerId());
        Transaction transaction = Transaction.builder()
            .amount(request.getAmount())
            .seller(seller)
            .paymentType(request.getPaymentType())
            .transactionDate(request.getTransactionDate() != null ? request.getTransactionDate() : LocalDateTime.now())
            .build();
        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("created new transaction with id {} for seller id {}", savedTransaction.getId(), seller.getId());
        return convertToDto(savedTransaction);
    }
}