package org.monkey_business.utility_supervisor.properties;

import lombok.Getter;
import lombok.Setter;
import org.monkey_business.utility_supervisor.limiter.RateLimiterService;
import org.monkey_business.utility_supervisor.telegram.BotMessageProcessor;
import org.monkey_business.utility_supervisor.telegram.PowerOutageBot;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "telegram.bot")
@Getter
@Setter
public class TelegramBotConfig {
    private String token;
    private String name;
    private List<String> chatIds;

    @Bean
    public TelegramBotsApi telegramBotsApi(PowerOutageBot bot) throws TelegramApiException {
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(bot);
        return api;
    }

    @Bean
    public PowerOutageBot bot(BotMessageProcessor processor, RateLimiterService rateLimiterService) {
        return new PowerOutageBot(name, token, processor, rateLimiterService);
    }
}
