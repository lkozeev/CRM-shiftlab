package ru.nsu.shift.crm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.nsu.shift.crm.entity.Seller;

@Repository
public interface SellerRepository extends JpaRepository<Seller, Long> {

    // Seller imp have @SQLRestriction("deleted = false") 

   boolean existsByName(String name);
}