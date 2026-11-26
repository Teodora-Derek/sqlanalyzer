package com.sofiauni.sqlanalyzer.demo;

import com.sofiauni.sqlanalyzer.demo.model.Customer;
import com.sofiauni.sqlanalyzer.demo.model.Order;
import com.sofiauni.sqlanalyzer.demo.model.OrderItem;
import com.sofiauni.sqlanalyzer.demo.model.Product;
import com.sofiauni.sqlanalyzer.demo.repository.CustomerRepository;
import com.sofiauni.sqlanalyzer.demo.repository.OrderRepository;
import com.sofiauni.sqlanalyzer.demo.repository.ProductRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Seeds a small realistic dataset so the demo endpoints cross the detector thresholds.
 */
@Component
public class DemoDataSeeder implements ApplicationRunner {

    private static final int CUSTOMERS = 3;
    private static final int PRODUCTS = 40;
    private static final int ORDERS = 12;
    private static final int ITEMS_PER_ORDER = 3;

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public DemoDataSeeder(CustomerRepository customerRepository,
                          ProductRepository productRepository,
                          OrderRepository orderRepository) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (orderRepository.count() > 0) {
            return;
        }

        List<Customer> customers = new ArrayList<>();
        for (int i = 0; i < CUSTOMERS; i++) {
            customers.add(customerRepository.save(new Customer("customer-" + i, "customer" + i + "@example.com")));
        }

        List<Product> products = new ArrayList<>();
        for (int i = 0; i < PRODUCTS; i++) {
            products.add(productRepository.save(new Product("product-" + i, BigDecimal.valueOf(10 + i))));
        }

        for (int i = 0; i < ORDERS; i++) {
            Order order = new Order(customers.get(i % customers.size()));
            for (int j = 0; j < ITEMS_PER_ORDER; j++) {
                order.addItem(new OrderItem(order, products.get((i + j) % products.size()), j + 1));
            }
            orderRepository.save(order);
        }
    }
}
