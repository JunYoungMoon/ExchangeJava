package com.mjy.coin.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        String value = p.getText();
        if (value == null || "null".equals(value)) {
            return null; // "null" 문자열 또는 실제 null 값을 처리
        }

        try {
            return LocalDateTime.parse(value); // 정상적인 경우 파싱
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Invalid format for LocalDateTime: " + value, e);
        }
    }
}