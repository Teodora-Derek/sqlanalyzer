package com.sofiauni.sqlanalyzer.demo.service;

import com.sofiauni.sqlanalyzer.demo.model.Order;
import com.sofiauni.sqlanalyzer.demo.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    public record OrderSummary(Long orderId, int itemCount) {
    }

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Triggers the N+1 anti-pattern: loads all orders, then lazily walks each order's items,
     * issuing one extra SELECT per order.
     */
    @Transactional(readOnly = true)
    public List<OrderSummary> listOrdersWithItemCounts() {
        List<OrderSummary> summaries = new ArrayList<>();
        for (Order order : orderRepository.findAll()) {
            summaries.add(new OrderSummary(order.getId(), order.getItems().size()));
        }
        return summaries;
    }

    /**
     * Triggers the repeated-identical-query anti-pattern: the same row is fetched twice within
     * one request instead of being reused.
     */
    @Transactional(readOnly = true)
    public int repeatedLookup(Long orderId) {
        boolean first = orderRepository.fetchById(orderId).isPresent();
        boolean second = orderRepository.fetchById(orderId).isPresent();
        return (first ? 1 : 0) + (second ? 1 : 0);
    }
}
