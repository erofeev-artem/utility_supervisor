package org.monkey_business.utility_supervisor.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:koltushi.yml", factory = YamlPropertySourceFactory.class)
@Setter
@Getter
public class KoltushiConfig {
    @Value("${koltushi.url}")
    public String url;

    @Value("${koltushi.announcementsPageParameter}")
    public String announcementsPageParameter;

    @Value("${koltushi.announcementPageParameter}")
    public String announcementPageParameter;

    @Value("${koltushi.targetTp}")
    public String targetTp;
}
