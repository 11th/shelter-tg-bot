package com.skypro.telegram_team.handler.buffers;

import com.skypro.telegram_team.handler.buffer.Request;
import com.skypro.telegram_team.handler.buffer.RequestsBuffer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RequestsBufferTest {
    private RequestsBuffer out;
    private Request expected;

    @BeforeEach
    void setUp() {
        out = new RequestsBuffer();
        expected = new Request(11L);
        expected.setUserPhoneRequested(true);
    }

    @Test
    void addRequest() {
        //When
        out.addRequest(expected);
        //Then
        Assertions.assertThat(out.getRequest(11L).isPresent()).isTrue();
        Assertions.assertThat(out.getRequest(11L).get()).isEqualTo(expected);
    }

    @Test
    void delRequest() {
        //Given
        out.addRequest(expected);
        //When
        out.delRequest(expected);
        //Then
        Assertions.assertThat(out.getRequest(11L).isPresent()).isFalse();
    }

    @Test
    void getRequest() {
        //Given
        out.addRequest(expected);
        //When
        var actual = out.getRequest(11L);
        //Then
        Assertions.assertThat(actual.isPresent()).isTrue();
        Assertions.assertThat(actual.get()).isEqualTo(expected);
    }
}