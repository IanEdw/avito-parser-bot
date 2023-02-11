package ru.ianedw.avitoparserclienttelegrambot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AvitoParserClientTelegramBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(AvitoParserClientTelegramBotApplication.class, args);
	}

}
