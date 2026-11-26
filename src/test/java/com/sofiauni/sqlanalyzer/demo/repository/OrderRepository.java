package com.sofiauni.sqlanalyzer.demo.repository;

import com.sofiauni.sqlanalyzer.demo.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * JPQL query (unlike {@code findById}) always issues SQL, so calling it twice within one
     * request reproduces the repeated-identical-query anti-pattern instead of being served from
     * the persistence context first-level cache.
     */
    @Query("select o from Order o where o.id = ?1")
    Optional<Order> fetchById(Long id);
}
