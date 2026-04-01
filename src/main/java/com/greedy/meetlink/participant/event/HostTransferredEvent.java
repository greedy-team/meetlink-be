package com.greedy.meetlink.participant.event;

public record HostTransferredEvent(String meetingCode, String newHostNickname) {}
