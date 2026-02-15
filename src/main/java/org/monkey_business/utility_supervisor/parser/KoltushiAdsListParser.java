package org.monkey_business.utility_supervisor.parser;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class KoltushiAdsListParser {

    private static final String TARGET_TEXT = "Перерыв электроснабжения";
    private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4})");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public List<String> parse(Document document) {
        Elements postContents = document.getElementsByClass("post-content");
        if (postContents.isEmpty()) {
            throw new RuntimeException("The page has no post-content blocks");
        }
        return postContents.stream()
                .flatMap(block -> block.select("h2.post-title a").stream())
                .filter(a -> a.text().contains(TARGET_TEXT) && !isDateOlderThanToday(a.text()))
                .map(a -> a.attr("href"))
                .toList();
    }

    private boolean isDateOlderThanToday(String text) {
        Matcher matcher = DATE_PATTERN.matcher(text);
        if (!matcher.find()) {
            return false;
        }
        LocalDate date = LocalDate.parse(matcher.group(1), DATE_FORMATTER);
        return date.isBefore(LocalDate.now().minusDays(3));
    }
}
