package org.monkey_business.utility_supervisor.telegram;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.monkey_business.utility_supervisor.dto.ResultOutageDto;
import org.monkey_business.utility_supervisor.dto.RossetiOutageResponseDto;
import org.monkey_business.utility_supervisor.enums.UserState;
import org.monkey_business.utility_supervisor.properties.RossetiConfig;
import org.monkey_business.utility_supervisor.request.RossetiRequest;
import org.monkey_business.utility_supervisor.service.RossetiService;
import org.monkey_business.utility_supervisor.service.StateMachineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class BotMessageProcessor {
    private final RossetiService rossetiService;
    private final StateMachineService stateMachineService;
    private final RossetiConfig rossetiConfig;
    private final String district;
    private final String defaultSettlement;

    @Autowired
    public BotMessageProcessor(RossetiService rossetiService, StateMachineService stateMachineService, RossetiConfig rossetiConfig) {
        this.rossetiService = rossetiService;
        this.stateMachineService = stateMachineService;
        this.rossetiConfig = rossetiConfig;
        district = rossetiConfig.getDistrict();
        defaultSettlement = rossetiConfig.getStreet();
    }

    public SendMessage processMessage(Update update) {
        log.info("process message");
        SendMessage message;
        String chatId = String.valueOf(update.getMessage().getChatId());
        String text = update.getMessage().getText();
        String currentState = stateMachineService.getCurrentState(chatId);
        if (currentState != null && currentState.equals(UserState.AWAITING_ADDRESS.toString())) {
            message = makeMessageBySettlement(chatId, text);
            message = addWelcomeKeyboard(message);
            stateMachineService.resetState(chatId);
        } else {
            stateMachineService.resetState(chatId);
            message = makeWelcomeMessage(String.valueOf(chatId));
        }
        return message;
    }

    public SendMessage processCallback(Update update) {
        log.info("process callback");
        String data = update.getCallbackQuery().getData();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        if ("set_location".equals(data)) {
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText("Введите название населенного пункта");
            stateMachineService.setUserState(chatId.toString(), UserState.AWAITING_ADDRESS.toString());
            return message;
        } else if ("next_date".equals(data)) {
            SendMessage message = makeMessageBySettlement(chatId.toString(), defaultSettlement, 1);
            return addWelcomeKeyboard(message);
        }
        return makeWelcomeMessage(chatId.toString());
    }

    public SendMessage makeWelcomeMessage(String chatId) {
        log.info("make welcome message");
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Выберите действие:");
        return addWelcomeKeyboard(message);
    }

    public SendMessage addWelcomeKeyboard(SendMessage message) {
        log.info("add welcome keybord");
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder().text("Узнать дату ближайшего отключения").callbackData("next_date").build());
        row2.add(InlineKeyboardButton.builder().text("Поиск по Всеволожскому району")
                .callbackData("set_location").build());
        keyboardRows.add(row1);
        keyboardRows.add(row2);
        keyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(keyboardMarkup);
        return message;
    }

    public SendMessage makeMessageBySettlement(String chatId, String settlement) {
        log.info("make message by settlement");
        LocalDate now = LocalDate.now();
        LocalDate plusMonth = now.plusMonths(1);
        RossetiRequest request = new RossetiRequest(district, now, plusMonth, settlement);
        ResultOutageDto<RossetiOutageResponseDto> resultOutageDto = rossetiService.find(request);
        SendMessage message = processMessageText(resultOutageDto, settlement);
        message.setChatId(chatId);
        return message;
    }

    public SendMessage makeMessageBySettlement(String chatId, String settlement, int limit) {
        log.info("make message by settlement with limit");
        LocalDate now = LocalDate.now();
        LocalDate plusMonth = now.plusMonths(1);
        RossetiRequest request = new RossetiRequest(district, now, plusMonth, settlement);
        ResultOutageDto<RossetiOutageResponseDto> resultOutageDto = rossetiService.find(request, limit);
        SendMessage message = processMessageText(resultOutageDto, settlement);
        message.setChatId(chatId);
        message.enableHtml(true);
        return message;
    }

    private SendMessage processMessageText(ResultOutageDto<RossetiOutageResponseDto> resultOutageDto, String settlement) {
        log.info("process message text");
        SendMessage message = new SendMessage();
        if (resultOutageDto.getStatusCode() != HttpStatus.SC_OK) {
            message.setText("Сервис временно недоступен, попробуйте позже");
        } else if (resultOutageDto.getData().size() > 0) {
            List<String> rossetiEvents = resultOutageDto.getData().stream().map(m -> m.getDistrict() + ", "
                    + m.getAddress() + ". Плановое отключение электричества " + m.getStartDate() + " с "
                    + m.getStartTime() + " по " + m.getEndDate() + " " + m.getEndTime()
                    + "\n\n").collect(Collectors.toList());
            message.setText(String.join("", rossetiEvents));
        } else {
            message.setText("Данные о плановых отключениях для " + settlement + " не найдены");
        }
        message.enableHtml(true);
        return message;
    }

    public SendMessage makeMessageForTomorrow(ResultOutageDto<RossetiOutageResponseDto> resultOutageDto) {
        log.info("make message for tomorrow");
        SendMessage message = new SendMessage();
        String startTime = resultOutageDto.getData().get(0).getStartTime();
        String endTime = resultOutageDto.getData().get(0).getEndTime();
        String startDate = resultOutageDto.getData().get(0).getStartDate();
        message.setText("На завтра '" + startDate + "' в д. Разметелево запланировано отключение электричества с " +
                startTime + " до " + endTime);
        return message;
    }

    public SendMessage makeMessageForToday(ResultOutageDto<RossetiOutageResponseDto> resultOutageDto) {
        log.info("make message for today");
        SendMessage message = new SendMessage();
        String startTime = resultOutageDto.getData().get(0).getStartTime();
        String endTime = resultOutageDto.getData().get(0).getEndTime();
        String startDate = resultOutageDto.getData().get(0).getStartDate();
        message.setText("Внимание! Сегодня '" + startDate + "' в д. Разметелево запланировано отключение электричества с " +
                startTime + " до " + endTime);
        return message;
    }
}
