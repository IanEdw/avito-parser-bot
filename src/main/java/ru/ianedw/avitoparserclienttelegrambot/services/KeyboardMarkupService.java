package ru.ianedw.avitoparserclienttelegrambot.services;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

@Component
public class KeyboardMarkupService {

    public ReplyKeyboardMarkup getMenuMarkup() {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        KeyboardRow row = new KeyboardRow();
        row.add("/addTarget");
        markup.setKeyboard(List.of(row));
        return markup;
    }
}
