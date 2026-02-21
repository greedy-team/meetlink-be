package com.greedy.meetlink.place.domain;

/**
 * 위경도 좌표 값 객체
 * - latitude: 위도 (Y축)
 * - longitude: 경도 (X축)
 *
 * TMap API 스펙:
 *   startX / endX = longitude (경도)
 *   startY / endY = latitude  (위도)
 */
public record Coordinate(double latitude, double longitude) {

    /**
     * Haversine 공식으로 두 좌표 간 구면 직선 거리(km) 계산
     */
    public double distanceTo(Coordinate other) {
        final double R = 6371.0; // 지구 반경 (km)
        double dLat = Math.toRadians(other.latitude() - this.latitude());
        double dLon = Math.toRadians(other.longitude() - this.longitude());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(this.latitude()))
                  * Math.cos(Math.toRadians(other.latitude()))
                  * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    /**
     * 기준 좌표로부터 거리(km)와 각도(degree)만큼 이동한 좌표 반환
     * Polar Sampling에서 후보 좌표 생성 시 사용
     */
    public Coordinate move(double distanceKm, double angleDegrees) {
        final double R = 6371.0;
        double angularDistance = distanceKm / R;
        double bearing = Math.toRadians(angleDegrees);

        double lat1 = Math.toRadians(this.latitude());
        double lon1 = Math.toRadians(this.longitude());

        double lat2 = Math.asin(
                Math.sin(lat1) * Math.cos(angularDistance)
                + Math.cos(lat1) * Math.sin(angularDistance) * Math.cos(bearing)
        );

        double lon2 = lon1 + Math.atan2(
                Math.sin(bearing) * Math.sin(angularDistance) * Math.cos(lat1),
                Math.cos(angularDistance) - Math.sin(lat1) * Math.sin(lat2)
        );

        return new Coordinate(Math.toDegrees(lat2), Math.toDegrees(lon2));
    }
}
