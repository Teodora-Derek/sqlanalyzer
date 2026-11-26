package com.sofiauni.sqlanalyzer.demo.service;

import com.sofiauni.sqlanalyzer.demo.model.Product;
import com.sofiauni.sqlanalyzer.demo.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class CatalogService {

    private final ProductRepository productRepository;

    public CatalogService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /** Triggers the missing-pagination anti-pattern: full unbounded SELECT. */
    @Transactional(readOnly = true)
    public int countAllProducts() {
        return productRepository.findAll().size();
    }

    /** Triggers SELECT_STAR (and missing pagination) via the native query. */
    @Transactional(readOnly = true)
    public int countAllProductsRaw() {
        return productRepository.findAllRaw().size();
    }

    /**
     * Triggers the bulk-instead-of-batch (and, for large counts, transaction-span) anti-pattern:
     * inserts rows one statement at a time within a single transaction.
     */
    @Transactional
    public void importProducts(int count) {
        for (int i = 0; i < count; i++) {
            Product product = new Product("imported-product-" + i, BigDecimal.valueOf(9.99));
            productRepository.saveAndFlush(product);
        }
    }
}
