package org.monkey_business.utility_supervisor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class UtilitySupervisorApplication {
	public static void main(String[] args) {
		SpringApplication.run(UtilitySupervisorApplication.class, args);
	}

}
