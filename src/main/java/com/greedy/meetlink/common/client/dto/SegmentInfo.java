package com.greedy.meetlink.common.client.dto;

import java.util.List;

public record SegmentInfo(String mode, List<List<Double>> coordinates) {}
