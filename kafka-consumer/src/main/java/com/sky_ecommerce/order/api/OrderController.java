package com.sky_ecommerce.order.api;

import com.sky_ecommerce.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> create(@RequestBody @Valid CreateOrderRequest req,
                                                      @RequestHeader(value = "X-Correlation-Id", required = false) String cid) {
        String id = service.createOrder(req, cid);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("orderId", id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable String id,
                                       @RequestHeader(value = "X-Correlation-Id", required = false) String cid,
                                       @RequestParam(defaultValue = "user-request") String reason) {
        service.cancel(id, cid, reason);
        return ResponseEntity.accepted().build();
    }
}
