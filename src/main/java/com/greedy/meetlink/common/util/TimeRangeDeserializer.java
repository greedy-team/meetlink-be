package com.greedy.meetlink.common.util;

import java.time.LocalTime;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

public class TimeRangeDeserializer extends ValueDeserializer<LocalTime> {
    @Override
    public LocalTime deserialize(JsonParser p, DeserializationContext ctx) {
        String text = p.getString().trim();
        return text.startsWith("24:") ? LocalTime.of(23, 59, 59) : LocalTime.parse(text);
    }
}
