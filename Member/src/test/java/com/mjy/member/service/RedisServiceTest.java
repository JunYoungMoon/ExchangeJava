package com.mjy.member.service;

import com.mjy.member.service.RedisService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.awaitility.Awaitility.await;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisServiceTest {
    final String KEY = "key";
    final String VALUE = "value";
    final Duration DURATION = Duration.ofMillis(5000);
    @Mock
    private RedisService redisService;

    @Test
    @DisplayName("Redis에 데이터를 저장하면 정상적으로 조회된다.")
    void saveAndFindTest() {
        when(redisService.getValues(KEY)).thenReturn(VALUE);

        // when
        String findValue = redisService.getValues(KEY);

        // then
        assertThat(VALUE).isEqualTo(findValue);
    }

    @Test
    @DisplayName("Redis에 저장된 데이터를 수정할 수 있다.")
    void updateTest() {
        // given
        String updateValue = "updateValue";
        when(redisService.getValues(KEY)).thenReturn(updateValue);

        // when
        redisService.setValues(KEY, updateValue, DURATION);
        String findValue = redisService.getValues(KEY);

        // then
        assertThat(updateValue).isEqualTo(findValue);
        assertThat(VALUE).isNotEqualTo(findValue);
    }

    @Test
    @DisplayName("Redis에 저장된 데이터를 삭제할 수 있다.")
    void deleteTest() {
        // given
        doAnswer(invocation -> {
            when(redisService.getValues(KEY)).thenReturn("false");
            return null;
        }).when(redisService).deleteValues(KEY);

        // when
        redisService.deleteValues(KEY);
        String findValue = redisService.getValues(KEY);

        // then
        assertThat(findValue).isEqualTo("false");
    }

    @Test
    @DisplayName("Redis에 저장된 데이터는 만료시간이 지나면 삭제된다.")
    void expiredTest() {
        // given
        when(redisService.getValues(KEY)).thenReturn(VALUE).thenReturn("false");

        // when
        String findValue = redisService.getValues(KEY);
        await().pollDelay(Duration.ofMillis(6000)).untilAsserted(
                () -> {
                    String expiredValue = redisService.getValues(KEY);
                    assertThat(expiredValue).isNotEqualTo(findValue);
                    assertThat(expiredValue).isEqualTo("false");
                }
        );
    }
}