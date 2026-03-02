package com.greedy.meetlink.common.util;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PolylineDecoder {
    private static final double PRECISION = 1e6;

    public static List<List<Double>> decode(String encoded) {
        if (encoded == null) {
            return List.of();
        }

        List<List<Double>> result = new ArrayList<>();
        int index = 0;
        int lat = 0;
        int lng = 0;

        while (index < encoded.length()) {
            int b;
            int shift = 0;
            int chunk = 0;

            do {
                b = encoded.charAt(index++) - 63;
                chunk |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);

            lat += (chunk & 1) != 0 ? ~(chunk >> 1) : (chunk >> 1);

            shift = 0;
            chunk = 0;

            do {
                b = encoded.charAt(index++) - 63;
                chunk |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);

            lng += (chunk & 1) != 0 ? ~(chunk >> 1) : (chunk >> 1);

            result.add(List.of(lat / PRECISION, lng / PRECISION));
        }

        return result;
    }
}
