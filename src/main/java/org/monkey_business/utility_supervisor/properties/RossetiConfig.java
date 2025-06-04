package org.monkey_business.utility_supervisor.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:rosseti.yml", factory = YamlPropertySourceFactory.class)
@Setter
@Getter
public class RossetiConfig {

    @Value("${url}")
    public String url;
    @Value("${city}")
    public String city;
    @Value("${street}")
    public String street;
}
