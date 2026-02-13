package com.greedy.meetlink.station;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor
public class Station {

    @Id
    private String id;       // 예: BS_1100_100000002
    private String name;     // 예: 창경궁.서울대학교병원
    private Double latitude; // 37.579433
    private Double longitude;// 126.996522

    public Station(String id, String name, Double latitude, Double longitude) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
