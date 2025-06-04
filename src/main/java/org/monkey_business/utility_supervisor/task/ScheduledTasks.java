package org.monkey_business.utility_supervisor.task;

import org.monkey_business.utility_supervisor.dto.ResultOutageDto;
import org.monkey_business.utility_supervisor.dto.RossetiOutageResponseDto;
import org.monkey_business.utility_supervisor.request.RossetiRequest;
import org.monkey_business.utility_supervisor.service.RossetiService;
import org.monkey_business.utility_supervisor.telegram.BotMessageProcessor;
import org.monkey_business.utility_supervisor.telegram.PowerOutageBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.LocalDate;

@Component
public class ScheduledTasks {
    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);
    @Autowired
    private final RossetiService rossetiService;
    @Autowired
    private final BotMessageProcessor processor;
    @Autowired
    private final PowerOutageBot bot;
    private final String district = "Всеволожский";
    private final String settlement = "Разметелево";
    private final String chatId = "-1002602035110";

//    @Autowired
    public ScheduledTasks(RossetiService rossetiService, BotMessageProcessor processor, PowerOutageBot bot) {
        this.rossetiService = rossetiService;
        this.processor = processor;
        this.bot = bot;
    }


    @Scheduled(cron = "0 0/2 * * * ?", zone="Europe/Moscow")
//    @Scheduled(cron = "0 */1 * * * *", zone = "Europe/Moscow")
    public void call() {
//        log.info("Get rosseti data", dateFormat.format(new Date()));
            LocalDate now = LocalDate.now();
            LocalDate plusDay = now.plusDays(1);
            RossetiRequest request = new RossetiRequest(district, now, plusDay, settlement);
        SendMessage message = processor.makeMessageBySettlement(chatId, settlement);
        bot.sendMessage(message);
        }

//    и в 9 утра
}
