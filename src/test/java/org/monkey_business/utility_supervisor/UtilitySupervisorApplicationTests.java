package org.monkey_business.utility_supervisor;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.telegram.telegrambots.meta.TelegramBotsApi;

@SpringBootTest
class UtilitySupervisorApplicationTests {

	@MockBean
	private TelegramBotsApi telegramBotsApi;

	@Test
	void contextLoads() {
	}

}
