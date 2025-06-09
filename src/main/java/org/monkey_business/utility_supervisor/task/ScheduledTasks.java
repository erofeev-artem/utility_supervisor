package org.monkey_business.utility_supervisor.task;

import org.apache.http.HttpStatus;
import org.monkey_business.utility_supervisor.dto.ResultOutageDto;
import org.monkey_business.utility_supervisor.dto.RossetiOutageResponseDto;
import org.monkey_business.utility_supervisor.properties.RossetiConfig;
import org.monkey_business.utility_supervisor.properties.TelegramBotConfig;
import org.monkey_business.utility_supervisor.request.RossetiRequest;
import org.monkey_business.utility_supervisor.service.RossetiService;
import org.monkey_business.utility_supervisor.storage.Storage;
import org.monkey_business.utility_supervisor.telegram.BotMessageProcessor;
import org.monkey_business.utility_supervisor.telegram.PowerOutageBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ScheduledTasks {
    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private final RossetiService rossetiService;
    private final BotMessageProcessor processor;
    private final PowerOutageBot bot;
    private final RossetiConfig rossetiConfig;
    private final TelegramBotConfig telegramBotConfig;
    private final String district;
    private final String settlement;
    private final List<String> chatIds;

    @Autowired
    public ScheduledTasks(RossetiService rossetiService, BotMessageProcessor processor, PowerOutageBot bot,
                          RossetiConfig rossetiConfig, TelegramBotConfig telegramBotConfig) {
        this.rossetiService = rossetiService;
        this.processor = processor;
        this.bot = bot;
        this.rossetiConfig = rossetiConfig;
        this.telegramBotConfig = telegramBotConfig;
        this.district = rossetiConfig.getDistrict();
        this.settlement = rossetiConfig.getStreet();
        this.chatIds = telegramBotConfig.getChatIds();
    }

    @Scheduled(cron = "0 0 18 * * ?", zone = "Europe/Moscow")
    public void callEvening() {
        log.info("Evening reminder schedule started");
        LocalDate starDate = LocalDate.now().plusDays(1);
        LocalDate endDate = starDate;
        RossetiRequest request = new RossetiRequest(district, starDate, endDate, settlement);
        ResultOutageDto<RossetiOutageResponseDto> resultOutageDto = rossetiService.find(request, 1);
        if (resultOutageDto.getData().size() > 0 && resultOutageDto.getStatusCode() == HttpStatus.SC_OK) {
            Storage.put(resultOutageDto.getData().get(0).getStartDate(), resultOutageDto);
            SendMessage message = processor.makeMessageForTomorrow(resultOutageDto);
            for (String chatId : chatIds) {
                log.info("callEvening chat id: " + chatId);
                message.setChatId(chatId);
                bot.sendMessage(message);
            }
        }
        log.info("Evening reminder schedule finished");
    }


    @Scheduled(cron = "0 0 8 * * ?", zone = "Europe/Moscow")
    public void callMorning() {
        log.info("Morning reminder schedule started");
        LocalDate date = LocalDate.now();
        String formattedDate = date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        log.info("Morning reminder - get result outage dto");
        ResultOutageDto<RossetiOutageResponseDto> resultOutageDto = Storage.get(formattedDate);
        if (resultOutageDto != null) {
            SendMessage message = processor.makeMessageForToday(resultOutageDto);
            for (String chatId : chatIds) {
                log.info("callMorning chat id: " + chatId);
                message.setChatId(chatId);
                bot.sendMessage(message);
            }
            Storage.remove(formattedDate);
        } else {
            log.info("Morning reminder - resultOutageDto is null");
        }
        log.info("Morning reminder schedule finished");
    }
}
