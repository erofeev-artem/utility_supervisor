package org.monkey_business.utility_supervisor.task;

import org.monkey_business.utility_supervisor.dto.KoltushiOutageResponseDto;
import org.monkey_business.utility_supervisor.config.TelegramBotConfig;
import org.monkey_business.utility_supervisor.service.KoltushiService;
import org.monkey_business.utility_supervisor.storage.KoltushiStorage;
import org.monkey_business.utility_supervisor.telegram.BotMessageProcessor;
import org.monkey_business.utility_supervisor.telegram.PowerOutageBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "scheduling.enabled", havingValue = "true")
public class ScheduledTasks {
    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private final KoltushiService koltushiService;
    private final KoltushiStorage koltushiStorage;
    private final BotMessageProcessor processor;
    private final PowerOutageBot bot;
    private final List<String> chatIds;

    @Autowired
    public ScheduledTasks(KoltushiService koltushiService, KoltushiStorage koltushiStorage,
                          BotMessageProcessor processor, PowerOutageBot bot,
                          TelegramBotConfig telegramBotConfig) {
        this.koltushiService = koltushiService;
        this.koltushiStorage = koltushiStorage;
        this.processor = processor;
        this.bot = bot;
        this.chatIds = telegramBotConfig.getChatIds();
    }

    @Scheduled(cron = "${scheduling.cron.update-koltushi}", zone = "Europe/Moscow")
    public void callEveningKoltushi() {
        log.info("Koltushi updating schedule started");
        List<KoltushiOutageResponseDto> outages = koltushiService.request();

        koltushiStorage.clear();
        Map<LocalDate, List<KoltushiOutageResponseDto>> grouped = outages.stream()
                .collect(Collectors.groupingBy(KoltushiOutageResponseDto::getDate));
        grouped.forEach(koltushiStorage::put);
        log.info("Stored {} outage dates in KoltushiStorage", grouped.size());

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

        log.info("Koltushi evening schedule finished");
    }
}