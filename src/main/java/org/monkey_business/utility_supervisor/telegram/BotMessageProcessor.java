package org.monkey_business.utility_supervisor.telegram;

import lombok.extern.slf4j.Slf4j;
import org.monkey_business.utility_supervisor.dto.KoltushiOutageResponseDto;
import org.monkey_business.utility_supervisor.storage.KoltushiStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class BotMessageProcessor {

    private final KoltushiStorage koltushiStorage;
    private final int forecastDays;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Autowired
    public BotMessageProcessor(KoltushiStorage koltushiStorage,
                               @org.springframework.beans.factory.annotation.Value("${koltushi.forecast-days:3}") int forecastDays) {
        this.koltushiStorage = koltushiStorage;
        this.forecastDays = forecastDays;
    }

    public SendMessage processMessage(Update update) {
        log.info("process message");
        String chatId = String.valueOf(update.getMessage().getChatId());
        return makeWelcomeMessage(chatId);
    }

    public SendMessage processCallback(Update update) {
        log.info("process callback");
        String data = update.getCallbackQuery().getData();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        if ("koltushi_outages".equals(data)) {
            return makeKoltushiOutagesMessage(chatId.toString());
        }
        if ("hint".equals(data)) {
            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());
            message.setText("ТП-7530 - очереди 1 и 2\nТП-5189 - очередь 3");
            return message;
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
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text("Отключения 1-3 очереди")
                .callbackData("koltushi_outages")
                .build());
        keyboardRows.add(row);
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(InlineKeyboardButton.builder()
                .text("Подсказка")
                .callbackData("hint")
                .build());
        keyboardRows.add(row2);
        keyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(keyboardMarkup);
        return message;
    }

    public SendMessage makeKoltushiWarningMessage(List<KoltushiOutageResponseDto> outages) {
        log.info("make koltushi warning message");
        SendMessage message = new SendMessage();
        String date = outages.get(0).getDate().format(DATE_FORMATTER);
        String tps = outages.stream()
                .flatMap(dto -> dto.getMatchedTps().stream())
                .distinct()
                .collect(Collectors.joining(", "));
        message.setText("Внимание! Завтра " + date + " запланировано отключение электроснабжения.\n" +
                "Затронутые подстанции: " + tps);
        return message;
    }

    public SendMessage makeKoltushiOutagesMessage(String chatId) {
        log.info("make koltushi outages message for next {} days", forecastDays);
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        List<KoltushiOutageResponseDto> outages = koltushiStorage.getForNextDays(forecastDays);
        if (outages.isEmpty()) {
            message.setText(String.format("Плановых отключений ТП-5189(очередь 3)\nТП-7530(очереди 1 и 2) на ближайшие %d дня не найдено.", forecastDays));
        } else {
            String text = outages.stream()
                    .map(dto ->
                             "\n" + dto.getDescription() + " " + String.join(", ", dto.getMatchedTps()))
                    .collect(Collectors.joining("\n\n"));
            message.setText("Плановые отключения ТП-5189(очередь 3)/ТП-7530(очередь 1 и 2):\n\n" + text);
        }
        return message;
    }
}