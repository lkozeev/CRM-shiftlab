package ru.nsu.shift.crm.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ru.nsu.shift.crm.entity.Seller;
import ru.nsu.shift.crm.entity.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Transaction imp have @SQLRestriction("deleted = false") 


    List<Transaction> findBySellerId(Long sellerId);

    List<Transaction> findByTransactionDateBetween(LocalDateTime start, LocalDateTime end);

    List<Transaction> findBySellerIdOrderByTransactionDateAsc(Long sellerId);
    
    /**
     * Returns the sellers with the highest total transaction amount in the period.
     * Use Pageable with limit 1 to get the top seller.
     */
    @Query("""
        SELECT t.seller
        FROM Transaction t
        WHERE t.transactionDate BETWEEN :start AND :end
        GROUP BY t.seller
        ORDER BY SUM(t.amount) DESC
        """)
    List<Seller> findTopSellersByTotalAmount(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end,
        Pageable pageable
    );

    /**
     * Returns the sellers whose total transaction amount in the period is below the specified threshold.
     * Results are ordered by total amount descending.
     */

    @Query("""
        SELECT t.seller
        FROM Transaction t
        WHERE t.transactionDate BETWEEN :start AND :end
        GROUP BY t.seller
        HAVING SUM(t.amount) < :threshold
        ORDER BY SUM(t.amount) DESC
        """)
    List<Seller> findSellersWithTotalBelow(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end,
        @Param("threshold") BigDecimal threshold
    );

}
