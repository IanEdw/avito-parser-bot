package ru.ianedw.telegrambots;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AvitoParserBot {

	public static void main(String[] args) {
		SpringApplication.run(AvitoParserBot.class, args);
	}

}
