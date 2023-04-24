package io.qmbot.telegrambot;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.DefaultBotOptions;

@Configuration
public class BotConfiguration {
    @Bean
    public DefaultBotOptions defaultBotOptions() {
     return new DefaultBotOptions();
    }
}
