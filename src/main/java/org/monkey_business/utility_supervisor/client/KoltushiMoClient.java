package org.monkey_business.utility_supervisor.client;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.monkey_business.utility_supervisor.exception.KoltushiClientException;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class KoltushiMoClient {

    public Document callAnnouncementPage(String url) {
        try {
            log.debug("Fetching URL: {}", url);
            return Jsoup.connect(url)
                    .timeout(10_000)
                    .userAgent("Mozilla/5.0 (compatible; UtilitySupervisor/1.0)")
                    .get();
        } catch (IOException e) {
            log.error("Failed to fetch announcement page from URL: {}", url, e);
            throw new KoltushiClientException("Failed to fetch page: " + url, e);
        }
    }
}
