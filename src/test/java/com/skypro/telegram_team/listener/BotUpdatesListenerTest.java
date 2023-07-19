package com.skypro.telegram_team.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.skypro.telegram_team.exception.InvalidDataException;
import com.skypro.telegram_team.handler.BotUpdateHandler;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BotUpdatesListenerTest {
    @Mock
    private TelegramBot telegramBot;
    @Mock
    private BotUpdateHandler updateListener;

    @InjectMocks
    private BotUpdatesListener out;

    @BeforeEach
    void setUp() {
        out = new BotUpdatesListener(telegramBot, updateListener);
    }

    @Test
    void process_ok() throws Exception {
        //Given
        Update update = BotListenerUtil.generateUpdate("/start");
        List<SendMessage> expected = Collections.singletonList(
                new SendMessage(update.message().chat().id(), update.message().text()));
        //When
        when(telegramBot.execute(any())).thenReturn(BotListenerUtil.generateResponseOk());
        when(updateListener.processUpdate(update)).thenReturn(expected);
        out.process(Collections.singletonList(update));
        //Then
        SendMessage actual = getActualSendMessage();
        Assertions.assertThat(actual.getParameters().get("chat_id")).isEqualTo(update.message().chat().id());
        Assertions.assertThat(actual.getParameters().get("text")).isEqualTo(update.message().text());
    }

    @Test
    void process_exception() throws Exception {
        //Given
        Update update = BotListenerUtil.generateUpdate("/start");
        //When
        when(updateListener.processUpdate(update)).thenThrow(new InvalidDataException("error"));
        out.process(Collections.singletonList(update));
        //Then
        Mockito.verify(telegramBot, times(0)).execute(any());
    }

    private SendMessage getActualSendMessage() {
        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
        Mockito.verify(telegramBot).execute(argumentCaptor.capture());
        return argumentCaptor.getValue();
    }
}