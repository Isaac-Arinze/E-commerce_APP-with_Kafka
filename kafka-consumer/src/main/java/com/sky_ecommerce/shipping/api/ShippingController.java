package com.sky_ecommerce.shipping.api;

import com.sky_ecommerce.shipping.service.ShippingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/shipping")
public class ShippingController {

    private final ShippingService shipping;

    public ShippingController(ShippingService shipping) {
        this.shipping = shipping;
    }

    /**
     * Simple quote that estimates delivery distance, ETA and fee from a central hub to the provided destination lat/lng.
     * Request body:
     * {
     *   "lat": 6.5,
     *   "lng": 3.4
     * }
     */
    @PostMapping("/quote")
    public ResponseEntity<Map<String, Object>> quote(@RequestBody Map<String, Object> body) {
        if (body == null || body.get("lat") == null || body.get("lng") == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "lat and lng are required"));
        }
        double lat = ((Number) body.get("lat")).doubleValue();
        double lng = ((Number) body.get("lng")).doubleValue();

        ShippingService.Quote q = shipping.quoteFromHub(lat, lng);
        return ResponseEntity.ok(Map.of(
                "distanceKm", q.distanceKm,
                "etaMinutes", q.etaMinutes,
                "fee", q.fee,
                "currency", "NGN"
        ));
    }
}
