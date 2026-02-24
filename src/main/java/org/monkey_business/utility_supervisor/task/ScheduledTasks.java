package org.monkey_business.utility_supervisor.task;

import lombok.extern.slf4j.Slf4j;
import org.monkey_business.utility_supervisor.dto.KoltushiOutageResponseDto;
import org.monkey_business.utility_supervisor.config.TelegramBotConfig;
import org.monkey_business.utility_supervisor.service.KoltushiService;
import org.monkey_business.utility_supervisor.telegram.BotMessageProcessor;
import org.monkey_business.utility_supervisor.telegram.PowerOutageBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(name = "scheduling.enabled", havingValue = "true")
public class ScheduledTasks {

    private final KoltushiService koltushiService;
    private final BotMessageProcessor processor;
    private final PowerOutageBot bot;
    private final List<String> chatIds;

    @Autowired
    public ScheduledTasks(KoltushiService koltushiService,
                          BotMessageProcessor processor, PowerOutageBot bot,
                          TelegramBotConfig telegramBotConfig) {
        this.koltushiService = koltushiService;
        this.processor = processor;
        this.bot = bot;
        this.chatIds = telegramBotConfig.getChatIds();
    }

    @Scheduled(cron = "${scheduling.cron.update-koltushi}", zone = "Europe/Moscow")
    public void updateKoltushi() {
        log.info("Koltushi updating schedule started");
        koltushiService.refreshStorage();
        log.info("Koltushi updating schedule completed");
    }

    @Scheduled(cron = "${scheduling.cron.warning-koltushi}", zone = "Europe/Moscow")
    public void warningKoltushi() {
        log.info("Koltushi check warning schedule started");
        List<KoltushiOutageResponseDto> outages = koltushiService.request();
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<KoltushiOutageResponseDto> tomorrowOutages = outages.stream()
                .filter(dto -> tomorrow.equals(dto.getDate()))
                .toList();

        if (!tomorrowOutages.isEmpty()) {
            log.info("Outage tomorrow found, sending Telegram warning");
            SendMessage message = processor.makeKoltushiWarningMessage(tomorrowOutages);
            for (String chatId : chatIds) {
                message.setChatId(chatId);
                bot.sendMessage(message);
            }
        }
        log.info("Koltushi warning schedule finished");
    }
}