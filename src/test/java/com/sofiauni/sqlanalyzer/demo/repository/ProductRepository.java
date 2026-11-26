package com.sofiauni.sqlanalyzer.demo.repository;

import com.sofiauni.sqlanalyzer.demo.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    /** Deliberately uses SELECT * to demonstrate the SELECT_STAR anti-pattern. */
    @Query(value = "SELECT * FROM product", nativeQuery = true)
    List<Product> findAllRaw();
}
