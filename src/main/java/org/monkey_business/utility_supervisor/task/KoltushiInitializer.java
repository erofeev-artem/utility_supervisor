package org.monkey_business.utility_supervisor.task;

import lombok.extern.slf4j.Slf4j;
import org.monkey_business.utility_supervisor.service.KoltushiService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KoltushiInitializer implements ApplicationRunner {

    private final KoltushiService koltushiService;

    public KoltushiInitializer(KoltushiService koltushiService) {
        this.koltushiService = koltushiService;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Koltushi initializer started");
        koltushiService.refreshStorage();
        log.info("Koltushi initialization completed");
    }
}