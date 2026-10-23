package com.project.code.Repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.code.Model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategory(String category);

    List<Product> findByPriceBetween(Double minPrice, Double maxPrice);

    List<Product> findBySku(String sku);

    Product findByName(String name);

    @Query("SELECT DISTINCT i.product FROM Inventory i "
            + "WHERE i.store.id = :storeId "
            + "AND LOWER(i.product.name) LIKE LOWER(CONCAT('%', :pname, '%'))")
    List<Product> findByNameLike(@Param("storeId") Long storeId, @Param("pname") String pname);

    @Query("SELECT DISTINCT i.product FROM Inventory i "
            + "WHERE i.store.id = :storeId "
            + "AND LOWER(i.product.name) LIKE LOWER(CONCAT('%', :pname, '%')) "
            + "AND LOWER(i.product.category) = LOWER(:category)")
    List<Product> findByNameAndCategory(
            @Param("storeId") Long storeId,
            @Param("pname") String pname,
            @Param("category") String category);

    @Query("SELECT DISTINCT i.product FROM Inventory i WHERE i.store.id = :storeId AND LOWER(i.product.category) = LOWER(:category)")
    List<Product> findByCategoryAndStoreId(
            @Param("storeId") Long storeId,
            @Param("category") String category);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :pname, '%'))")
    List<Product> findProductBySubName(@Param("pname") String pname);

    @Query("SELECT DISTINCT i.product FROM Inventory i WHERE i.store.id = :storeId")
    List<Product> findProductsByStoreId(@Param("storeId") Long storeId);

    @Query("SELECT DISTINCT i.product FROM Inventory i WHERE LOWER(i.product.category) = LOWER(:category) AND i.store.id = :storeId")
    List<Product> findProductByCategory(
            @Param("category") String category,
            @Param("storeId") Long storeId);

    @Query("SELECT p FROM Product p "
            + "WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :pname, '%')) "
            + "AND LOWER(p.category) = LOWER(:category)")
    List<Product> findProductBySubNameAndCategory(
            @Param("pname") String pname,
            @Param("category") String category);
}
