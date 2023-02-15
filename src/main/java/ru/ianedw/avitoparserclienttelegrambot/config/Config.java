package ru.ianedw.avitoparserclienttelegrambot.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.net.http.HttpClient;

@Configuration
@PropertySource("classpath:application.properties")
public class Config {
    private final Environment environment;

    @Autowired
    public Config(Environment environment) {
        this.environment = environment;
    }


    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public TelegramBotsApi telegramBotsApi() throws TelegramApiException {
        return new TelegramBotsApi(DefaultBotSession.class);
    }

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newHttpClient();
    }

    public String getBotName() {
        return environment.getProperty("bot.name");
    }

    public String getToken() {
        return environment.getProperty("bot.token");
    }

    public String getParserUrl() {
        return environment.getProperty("parser.url");
    }
}
