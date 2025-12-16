package com.specialtyfood.repository;

import com.specialtyfood.model.Address;
import com.specialtyfood.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Address entity operations
 */
@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    
    /**
     * Find addresses by user
     */
    List<Address> findByUserOrderByIsDefaultDescCreatedAtDesc(User user);
    
    /**
     * Find addresses by user ID
     */
    List<Address> findByUserIdOrderByIsDefaultDescCreatedAtDesc(Long userId);
    
    /**
     * Find default address for user
     */
    Optional<Address> findByUserAndIsDefaultTrue(User user);
    
    /**
     * Find default address by user ID
     */
    Optional<Address> findByUserIdAndIsDefaultTrue(Long userId);
    
    /**
     * Check if user has default address
     */
    boolean existsByUserAndIsDefaultTrue(User user);
    
    /**
     * Check if user has default address by user ID
     */
    boolean existsByUserIdAndIsDefaultTrue(Long userId);
    
    /**
     * Count addresses for user
     */
    @Query("SELECT COUNT(a) FROM Address a WHERE a.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);
    
    /**
     * Find addresses by city
     */
    List<Address> findByCityOrderByCreatedAtDesc(String city);
    
    /**
     * Find addresses by province
     */
    List<Address> findByProvinceOrderByCreatedAtDesc(String province);
    
    /**
     * Find addresses by country
     */
    List<Address> findByCountryOrderByCreatedAtDesc(String country);
    
    /**
     * Find addresses by postal code
     */
    List<Address> findByPostalCodeOrderByCreatedAtDesc(String postalCode);
    
    /**
     * Search addresses by full name or phone number
     */
    @Query("SELECT a FROM Address a WHERE " +
           "LOWER(a.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "a.phoneNumber LIKE CONCAT('%', :searchTerm, '%')")
    List<Address> searchAddresses(@Param("searchTerm") String searchTerm);
    
    /**
     * Search addresses for specific user
     */
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND " +
           "(LOWER(a.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "a.phoneNumber LIKE CONCAT('%', :searchTerm, '%') OR " +
           "LOWER(a.addressLine1) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.city) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Address> searchUserAddresses(@Param("userId") Long userId, @Param("searchTerm") String searchTerm);
    
    /**
     * Clear default flag for all user addresses
     */
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId")
    void clearDefaultFlagForUser(@Param("userId") Long userId);
    
    /**
     * Set address as default and clear others
     */
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = CASE WHEN a.id = :addressId THEN true ELSE false END " +
           "WHERE a.user.id = :userId")
    void setDefaultAddress(@Param("userId") Long userId, @Param("addressId") Long addressId);
    
    /**
     * Delete addresses by user
     */
    @Modifying
    @Query("DELETE FROM Address a WHERE a.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
    
    /**
     * Find addresses used in orders
     */
    @Query("SELECT DISTINCT a FROM Address a JOIN a.orders o")
    List<Address> findAddressesUsedInOrders();
    
    /**
     * Find addresses used in orders for specific user
     */
    @Query("SELECT DISTINCT a FROM Address a JOIN a.orders o WHERE a.user.id = :userId")
    List<Address> findAddressesUsedInOrdersByUserId(@Param("userId") Long userId);
    
    /**
     * Find unused addresses (not used in any orders)
     */
    @Query("SELECT a FROM Address a WHERE a.id NOT IN " +
           "(SELECT DISTINCT o.shippingAddress.id FROM Order o WHERE o.shippingAddress IS NOT NULL)")
    List<Address> findUnusedAddresses();
    
    /**
     * Find addresses by region (city and province)
     */
    @Query("SELECT a FROM Address a WHERE LOWER(a.city) = LOWER(:city) AND LOWER(a.province) = LOWER(:province)")
    List<Address> findByRegion(@Param("city") String city, @Param("province") String province);
    
    /**
     * Get address statistics by province
     */
    @Query("SELECT a.province, COUNT(a) FROM Address a GROUP BY a.province ORDER BY COUNT(a) DESC")
    List<Object[]> getAddressStatsByProvince();
    
    /**
     * Get address statistics by city
     */
    @Query("SELECT a.city, a.province, COUNT(a) FROM Address a " +
           "GROUP BY a.city, a.province ORDER BY COUNT(a) DESC")
    List<Object[]> getAddressStatsByCity();
}