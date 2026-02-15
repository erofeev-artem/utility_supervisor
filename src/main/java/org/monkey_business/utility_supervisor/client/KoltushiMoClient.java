package org.monkey_business.utility_supervisor.client;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.monkey_business.utility_supervisor.helper.SSLHelper;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class KoltushiMoClient {

    public Document callAnnouncementPage(String url) {
        Document document;
        try {
            document = SSLHelper.getConnection(url).get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return document;
    }
}
