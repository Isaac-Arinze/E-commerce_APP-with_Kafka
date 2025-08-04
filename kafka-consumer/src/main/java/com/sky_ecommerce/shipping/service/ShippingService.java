package com.sky_ecommerce.shipping.service;

import org.springframework.stereotype.Service;

@Service
public class ShippingService {

    // Lagos center (example hub) for distance estimates if no seller location is known.
    private static final double HUB_LAT = 6.465422;
    private static final double HUB_LNG = 3.406448;

    public static class Quote {
        public final double distanceKm;
        public final int etaMinutes;
        public final long fee; // in NGN kobo-like smallest unit or just NGN integer

        public Quote(double distanceKm, int etaMinutes, long fee) {
            this.distanceKm = distanceKm;
            this.etaMinutes = etaMinutes;
            this.fee = fee;
        }
    }

    /**
     * Haversine distance in kilometers.
     */
    public double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0088; // Earth radius in KM
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                        Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /**
     * Simple fee model:
     * - Base fee: 800 NGN
     * - Per km: 120 NGN/km after the first 3 km
     * - Cap: 20,000 NGN
     * ETA estimate:
     * - 30 km/h average => minutes = distanceKm / 30 * 60
     */
    public Quote quoteFromHub(double dropoffLat, double dropoffLng) {
        double dist = distanceKm(HUB_LAT, HUB_LNG, dropoffLat, dropoffLng);
        long base = 800;
        double chargeableKm = Math.max(0, dist - 3.0);
        long variable = Math.round(chargeableKm * 120);
        long fee = Math.min(20000, base + variable);
        int etaMin = Math.max(10, (int) Math.round(dist / 30.0 * 60.0));
        return new Quote(dist, etaMin, fee);
    }
}
