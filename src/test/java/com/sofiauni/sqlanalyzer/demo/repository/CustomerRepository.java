package com.sofiauni.sqlanalyzer.demo.repository;

import com.sofiauni.sqlanalyzer.demo.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
