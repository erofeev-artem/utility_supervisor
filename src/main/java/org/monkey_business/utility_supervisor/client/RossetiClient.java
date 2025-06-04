package org.monkey_business.utility_supervisor.client;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.monkey_business.utility_supervisor.helper.SSLHelper;
import org.monkey_business.utility_supervisor.request.RossetiRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class RossetiClient implements WebClient {

    /*
    city - это административный район
    street - это Населенный пункт / Адрес
     */
    public Document call(RossetiRequest request, String url) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        Document document;
        try {
            document = SSLHelper.getConnection(url)
                    .data("city", request.city(),
                            "date_start", request.startDate().format(dateTimeFormatter),
                            "date_finish", request.finishDate().format(dateTimeFormatter),
                            "street", request.street()).get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return document;
    }
}
