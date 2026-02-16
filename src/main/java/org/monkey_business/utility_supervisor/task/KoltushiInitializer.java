package org.monkey_business.utility_supervisor.task;

import org.monkey_business.utility_supervisor.dto.KoltushiOutageResponseDto;
import org.monkey_business.utility_supervisor.service.KoltushiService;
import org.monkey_business.utility_supervisor.storage.KoltushiStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class KoltushiInitializer {

    private static final Logger log = LoggerFactory.getLogger(KoltushiInitializer.class);

    private final KoltushiService koltushiService;
    private final KoltushiStorage koltushiStorage;

    public KoltushiInitializer(KoltushiService koltushiService, KoltushiStorage koltushiStorage) {
        this.koltushiService = koltushiService;
        this.koltushiStorage = koltushiStorage;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        log.info("Koltushi initializer started");
        List<KoltushiOutageResponseDto> outages = koltushiService.request();
        Map<LocalDate, List<KoltushiOutageResponseDto>> grouped = outages.stream()
                .collect(Collectors.groupingBy(KoltushiOutageResponseDto::getDate));
        grouped.forEach(koltushiStorage::put);
        log.info("KoltushiStorage populated with {} outage dates on startup", grouped.size());
    }
}