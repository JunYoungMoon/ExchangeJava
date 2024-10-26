package com.mjy.coin.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class CustomListJsonDeserializer<T> implements Deserializer<List<T>> {

    private final ObjectMapper objectMapper;
    private final TypeReference<List<T>> listTypeReference;

    public CustomListJsonDeserializer(ObjectMapper objectMapper, TypeReference<List<T>> listTypeReference) {
        this.objectMapper = objectMapper;
        this.listTypeReference = listTypeReference;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {}

    @Override
    public List<T> deserialize(String topic, byte[] data) {
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

