package org.monkey_business.utility_supervisor.telegram;

import lombok.extern.slf4j.Slf4j;
import org.monkey_business.utility_supervisor.limiter.RateLimiterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
public class PowerOutageBot extends TelegramLongPollingBot {

    private final String name;
    private final String token;
    private final BotMessageProcessor processor;
    private final RateLimiterService rateLimiterService;

    @Autowired
    public PowerOutageBot(String name, String token, BotMessageProcessor processor, RateLimiterService rateLimiterService) {
        this.name = name;
        this.token = token;
        this.processor = processor;
        this.rateLimiterService = rateLimiterService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery() && !update.getCallbackQuery().getMessage().isSuperGroupMessage() &&
                !update.getCallbackQuery().getMessage().isGroupMessage()) {
            Long userId = update.getCallbackQuery().getMessage().getChatId();
            if (!rateLimiterService.allowRequest(String.valueOf(userId))) {
                SendMessage message = new SendMessage();
                message.setChatId(userId);
                message.setText("Слишком много запросов, попробуйте через минуту");
                sendMessage(message);
            } else if (update.hasCallbackQuery()) {
                SendMessage message = processor.processCallback(update);
                sendMessage(message);

            }
        } else if (update.hasMessage() && !update.getMessage().isGroupMessage() && !update.getMessage().isSuperGroupMessage()) {
            Long userId = update.getMessage().getChatId();
            if (!rateLimiterService.allowRequest(String.valueOf(userId))) {
                SendMessage message = new SendMessage();
                message.setChatId(userId);
                message.setText("Слишком много запросов, попробуйте через минуту");
                sendMessage(message);
            } else if (update.hasMessage() && update.getMessage().hasText()) {
                SendMessage message = processor.processMessage(update);
                sendMessage(message);
            }
        }
    }

    @Override
    public String getBotUsername() {
        return name;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    public void sendMessage(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }
}
