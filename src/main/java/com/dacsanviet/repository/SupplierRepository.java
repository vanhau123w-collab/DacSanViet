package com.dacsanviet.repository;

import com.dacsanviet.model.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Supplier entity operations
 */
@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    
    /**
     * Find supplier by name
     */
    Optional<Supplier> findByName(String name);
    
    /**
     * Check if supplier name exists
     */
    boolean existsByName(String name);
    
    /**
     * Find active suppliers
     */
    List<Supplier> findByIsActiveTrueOrderByNameAsc();
    
    /**
     * Find suppliers by active status
     */
    Page<Supplier> findByIsActiveOrderByNameAsc(Boolean isActive, Pageable pageable);
    
    /**
     * Search suppliers by name or contact person
     */
    @Query("SELECT s FROM Supplier s WHERE " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.contactPerson) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Supplier> searchSuppliers(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Count active suppliers
     */
    @Query("SELECT COUNT(s) FROM Supplier s WHERE s.isActive = true")
    Long countActiveSuppliers();
    
    /**
     * Find all suppliers ordered by name
     */
    List<Supplier> findAllByOrderByNameAsc();
}
