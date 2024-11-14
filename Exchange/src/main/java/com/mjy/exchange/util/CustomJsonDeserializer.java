package com.mjy.exchange.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;
import java.util.Map;

public class CustomJsonDeserializer<T> implements Deserializer<T> {

    private final ObjectMapper objectMapper;
    private final TypeReference<T> listTypeReference;

    public CustomJsonDeserializer(ObjectMapper objectMapper, TypeReference<T> listTypeReference) {
        this.objectMapper = objectMapper;
        this.listTypeReference = listTypeReference;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {}

    @Override
    public T deserialize(String topic, byte[] data) {
        try {
            // JSON 배열을 리스트로 역직렬화
            return objectMapper.readValue(data, listTypeReference);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize JSON", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {}
}

