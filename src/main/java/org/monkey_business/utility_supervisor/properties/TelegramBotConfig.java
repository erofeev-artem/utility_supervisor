package org.monkey_business.utility_supervisor.properties;

import org.monkey_business.utility_supervisor.telegram.BotMessageProcessor;
import org.monkey_business.utility_supervisor.telegram.PowerOutageBot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class TelegramBotConfig {
    @Value("${telegram.bot.token}")
    private String token;

    @Value("${telegram.bot.name}")
    private String name;

    @Bean
    public TelegramBotsApi telegramBotsApi(PowerOutageBot bot) throws TelegramApiException {
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(bot);
        return api;
    }

    @Bean
    public PowerOutageBot bot(BotMessageProcessor processor) {
        return new PowerOutageBot(name, token, processor);
    }
}
