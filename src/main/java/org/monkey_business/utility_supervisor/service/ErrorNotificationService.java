package org.monkey_business.utility_supervisor.service;

import lombok.extern.slf4j.Slf4j;
import org.monkey_business.utility_supervisor.telegram.PowerOutageBot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class ErrorNotificationService {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final PowerOutageBot bot;
    private final String emergencyChat;

    public ErrorNotificationService(PowerOutageBot bot,
                                   @Value("${telegram.bot.emergency-chat}") String emergencyChat) {
        this.bot = bot;
        this.emergencyChat = emergencyChat;
    }

    public void sendScheduledTaskError(String taskName, Throwable throwable) {
        String message = formatScheduledTaskError(taskName, throwable);
        sendErrorMessage(message);
    }

    public void sendRestControllerError(String endpoint, String method, Throwable throwable) {
        String message = formatRestControllerError(endpoint, method, throwable);
        sendErrorMessage(message);
    }

    private String formatScheduledTaskError(String taskName, Throwable throwable) {
        return String.format(
                "⚠️ *Scheduled Task Error*\n\n" +
                "⏰ Time: %s\n" +
                "📋 Task: `%s`\n" +
                "❌ Error: `%s`\n" +
                "💬 Message: %s\n\n" +
                "Stack Trace:\n```\n%s\n```",
                LocalDateTime.now().format(DATE_FORMATTER),
                taskName,
                throwable.getClass().getSimpleName(),
                throwable.getMessage() != null ? throwable.getMessage() : "No message",
                getStackTracePreview(throwable)
        );
    }

    private String formatRestControllerError(String endpoint, String method, Throwable throwable) {
        return String.format(
                "⚠️ *REST Controller Error*\n\n" +
                "⏰ Time: %s\n" +
                "🌐 Endpoint: `%s %s`\n" +
                "❌ Error: `%s`\n" +
                "💬 Message: %s\n\n" +
                "Stack Trace:\n```\n%s\n```",
                LocalDateTime.now().format(DATE_FORMATTER),
                method,
                endpoint,
                throwable.getClass().getSimpleName(),
                throwable.getMessage() != null ? throwable.getMessage() : "No message",
                getStackTracePreview(throwable)
        );
    }

    private void sendErrorMessage(String message) {
        // Telegram message limit is 4096 characters
        if (message.length() > 4096) {
            message = message.substring(0, 4090) + "\n...\n```";
        }

        SendMessage sendMessage = SendMessage.builder()
                .chatId(emergencyChat)
                .text(message)
                .parseMode("Markdown")
                .build();

        bot.sendMessage(sendMessage);
        log.info("Error notification sent to Telegram chat: {}", emergencyChat);
    }

    private String getStackTracePreview(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        String fullStackTrace = sw.toString();

        // Limit stack trace to first 10 lines
        String[] lines = fullStackTrace.split("\n");
        int linesToShow = Math.min(10, lines.length);
        StringBuilder preview = new StringBuilder();
        for (int i = 0; i < linesToShow; i++) {
            preview.append(lines[i]).append("\n");
        }
        if (lines.length > 10) {
            preview.append("... (").append(lines.length - 10).append(" more lines)");
        }
        return preview.toString();
    }
}