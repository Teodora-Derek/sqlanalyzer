package com.sofiauni.sqlanalyzer.demo.web;

import com.sofiauni.sqlanalyzer.demo.service.CatalogService;
import com.sofiauni.sqlanalyzer.demo.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Demo endpoints, each deliberately exercising one inefficient access pattern so the analyzer has
 * something to detect. This controller is part of the test harness and is not shipped with the library.
 */
@RestController
@RequestMapping("/api/demo")
public class DemoController {

    private final OrderService orderService;
    private final CatalogService catalogService;

    public DemoController(OrderService orderService, CatalogService catalogService) {
        this.orderService = orderService;
        this.catalogService = catalogService;
    }

    @GetMapping("/orders")
    public List<OrderService.OrderSummary> orders() {
        return orderService.listOrdersWithItemCounts();
    }

    @GetMapping("/orders/{id}/repeated")
    public Map<String, Integer> repeated(@PathVariable Long id) {
        return Map.of("itemCount", orderService.repeatedLookup(id));
    }

    @GetMapping("/products")
    public Map<String, Integer> products() {
        return Map.of("count", catalogService.countAllProducts());
    }

    @GetMapping("/products/raw")
    public Map<String, Integer> productsRaw() {
        return Map.of("count", catalogService.countAllProductsRaw());
    }

    @PostMapping("/import")
    public Map<String, Integer> importProducts(@RequestParam(defaultValue = "60") int count) {
        catalogService.importProducts(count);
        return Map.of("imported", count);
    }
}
