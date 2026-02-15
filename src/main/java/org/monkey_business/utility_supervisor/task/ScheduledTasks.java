package org.monkey_business.utility_supervisor.task;

import org.apache.http.HttpStatus;
import org.monkey_business.utility_supervisor.dto.KoltushiOutageResponseDto;
import org.monkey_business.utility_supervisor.dto.RossetiResultOutageDto;
import org.monkey_business.utility_supervisor.dto.RossetiOutageResponseDto;
import org.monkey_business.utility_supervisor.config.RossetiConfig;
import org.monkey_business.utility_supervisor.config.TelegramBotConfig;
import org.monkey_business.utility_supervisor.request.RossetiRequest;
import org.monkey_business.utility_supervisor.service.KoltushiService;
import org.monkey_business.utility_supervisor.service.RossetiService;
import org.monkey_business.utility_supervisor.storage.KoltushiStorage;
import org.monkey_business.utility_supervisor.storage.RossetiStorage;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "scheduling.enabled", havingValue = "true")
public class ScheduledTasks {
    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private final RossetiService rossetiService;
    private final KoltushiService koltushiService;
    private final KoltushiStorage koltushiStorage;
    private final BotMessageProcessor processor;
    private final PowerOutageBot bot;
    private final RossetiConfig rossetiConfig;
    private final TelegramBotConfig telegramBotConfig;
    private final String district;
    private final String settlement;
    private final List<String> chatIds;

    @Autowired
    public ScheduledTasks(RossetiService rossetiService, BotMessageProcessor processor, PowerOutageBot bot,
                          RossetiConfig rossetiConfig, TelegramBotConfig telegramBotConfig,
                          KoltushiService koltushiService, KoltushiStorage koltushiStorage) {
        this.rossetiService = rossetiService;
        this.processor = processor;
        this.bot = bot;
        this.rossetiConfig = rossetiConfig;
        this.telegramBotConfig = telegramBotConfig;
        this.koltushiService = koltushiService;
        this.koltushiStorage = koltushiStorage;
        this.district = rossetiConfig.getDistrict();
        this.settlement = rossetiConfig.getStreet();
        this.chatIds = telegramBotConfig.getChatIds();
    }

    @Scheduled(cron = "${scheduling.cron.evening}", zone = "Europe/Moscow")
    public void callEveningRosseti() {
        log.info("Evening reminder schedule started");
        LocalDate starDate = LocalDate.now().plusDays(1);
        LocalDate endDate = starDate;
        RossetiRequest request = new RossetiRequest(district, starDate, endDate, settlement);
        RossetiResultOutageDto<RossetiOutageResponseDto> rossetiResultOutageDto = rossetiService.find(request, 1);
        if (rossetiResultOutageDto.getData().size() > 0 && rossetiResultOutageDto.getStatusCode() == HttpStatus.SC_OK) {
            RossetiStorage.put(rossetiResultOutageDto.getData().get(0).getStartDate(), rossetiResultOutageDto);
            SendMessage message = processor.makeMessageForTomorrow(rossetiResultOutageDto);
            for (String chatId : chatIds) {
                log.info("callEvening chat id: " + chatId);
                message.setChatId(chatId);
                bot.sendMessage(message);
            }
        }
        log.info("Evening reminder schedule finished");
    }

    @Scheduled(cron = "${scheduling.cron.morning}", zone = "Europe/Moscow")
    public void callMorningRosseti() {
        log.info("Morning reminder schedule started");
        LocalDate date = LocalDate.now();
        String formattedDate = date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        log.info("Morning reminder - get result outage dto");
        RossetiResultOutageDto<RossetiOutageResponseDto> rossetiResultOutageDto = RossetiStorage.get(formattedDate);
        if (rossetiResultOutageDto != null) {
            SendMessage message = processor.makeMessageForToday(rossetiResultOutageDto);
            for (String chatId : chatIds) {
                log.info("callMorning chat id: " + chatId);
                message.setChatId(chatId);
                bot.sendMessage(message);
            }
            RossetiStorage.remove(formattedDate);
        } else {
            log.info("Morning reminder - resultOutageDto is null");
        }
        log.info("Morning reminder schedule finished");
    }

    @Scheduled(cron = "${scheduling.cron.evening-koltushi}", zone = "Europe/Moscow")
    public void callEveningKoltushi() {
        log.info("Koltushi evening schedule started");

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