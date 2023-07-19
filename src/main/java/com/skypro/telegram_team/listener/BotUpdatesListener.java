package com.skypro.telegram_team.listener;


import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.skypro.telegram_team.handler.BotUpdateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class BotUpdatesListener implements UpdatesListener {

    private final Logger logger = LoggerFactory.getLogger(BotUpdatesListener.class);
    private final TelegramBot telegramBot;
    private final BotUpdateHandler updateListener;

    public BotUpdatesListener(TelegramBot telegramBot, BotUpdateHandler updateListener) {
        this.telegramBot = telegramBot;
        this.updateListener = updateListener;
    }

    /**
     * Инициализирует компонент, устанавливая этот экземпляр в качестве слушателя обновлений телеграм-бота.
     */
    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    /**
     * Обрабатывает список обновлений, используя сервис клавиатур.
     *
     * @param updates Список обновлений, которые необходимо обработать.
     * @return Код подтверждения для всех обновлений.
     */
    @Override
    public int process(List<Update> updates) {
        processUpdates(updates);
        return CONFIRMED_UPDATES_ALL;
    }

    /**
     * Обрабатывает список обновлений
     *
     * @param updates Список обновлений, которые необходимо обработать.
     */
    private void processUpdates(List<Update> updates) {
        updates.forEach(update -> {
            try {
                logger.info("Process update: {}", update);
                processUpdate(update);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        });
    }

    /**
     * Обрабатывает одно обновление из списка
     *
     * @param update Обновление, которое необходимо обработать
     */
    private void processUpdate(Update update) {
        updateListener.processUpdate(update)
                .forEach(telegramBot::execute);
    }
}



