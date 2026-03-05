package com.greedy.meetlink.common.client.dto;

import java.util.List;

public record RouteInfo(double travelTime, List<SegmentInfo> segments) {}
