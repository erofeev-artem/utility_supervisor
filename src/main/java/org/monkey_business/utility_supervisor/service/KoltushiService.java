package org.monkey_business.utility_supervisor.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.monkey_business.utility_supervisor.client.KoltushiMoClient;
import org.monkey_business.utility_supervisor.config.KoltushiConfig;
import org.monkey_business.utility_supervisor.dto.KoltushiOutageResponseDto;
import org.monkey_business.utility_supervisor.parser.KoltushiAdsListParser;
import org.monkey_business.utility_supervisor.parser.KoltushiOutagePageParser;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class KoltushiService {

    private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4})");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final KoltushiMoClient koltushiMoClient;
    private final KoltushiConfig koltushiConfig;
    private final KoltushiAdsListParser koltushiAdsListParser;
    private final KoltushiOutagePageParser koltushiOutagePageParser;

    public KoltushiService(KoltushiMoClient koltushiMoClient, KoltushiConfig koltushiConfig,
                           KoltushiAdsListParser koltushiAdsListParser, KoltushiOutagePageParser koltushiOutagePageParser) {
        this.koltushiMoClient = koltushiMoClient;
        this.koltushiConfig = koltushiConfig;
        this.koltushiAdsListParser = koltushiAdsListParser;
        this.koltushiOutagePageParser = koltushiOutagePageParser;
    }

    public List<KoltushiOutageResponseDto> request() {
        Document listingPage = koltushiMoClient.callAnnouncementPage(
                koltushiConfig.getUrl() + koltushiConfig.announcementsPageParameter);
        log.info("Fetched announcements listing page");

        List<String> hrefs = koltushiAdsListParser.parse(listingPage);
        log.info("Found {} outage announcements", hrefs.size());

        List<KoltushiOutageResponseDto> results = hrefs.stream()
                .flatMap(href -> {
                    Document postPage = koltushiMoClient.callAnnouncementPage(href);
                    Map<String, List<String>> outages = koltushiOutagePageParser.parse(postPage);
                    return outages.entrySet().stream()
                            .map(entry -> KoltushiOutageResponseDto.builder()
                                    .href(href)
                                    .date(extractDate(entry.getKey()))
                                    .description(entry.getKey())
                                    .matchedTps(entry.getValue())
                                    .build())
                            .filter(dto -> dto.getDate() != null);
                })
                .toList();

        log.info("Found {} matching outages", results.size());
        return results;
    }

    private LocalDate extractDate(String text) {
        Matcher matcher = DATE_PATTERN.matcher(text);
        if (!matcher.find()) return null;
        return LocalDate.parse(matcher.group(1), DATE_FORMATTER);
    }
}