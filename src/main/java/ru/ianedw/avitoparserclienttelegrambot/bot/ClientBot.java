package ru.ianedw.avitoparserclienttelegrambot.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.ianedw.avitoparserclienttelegrambot.config.Config;
import ru.ianedw.avitoparserclienttelegrambot.models.Person;
import ru.ianedw.avitoparserclienttelegrambot.services.BotService;

@Component
public class ClientBot extends TelegramLongPollingBot {
    private final BotService botService;
    private final String name;
    private final String token;


    @Autowired
    public ClientBot(BotService botService, Config config, TelegramBotsApi telegramBotsApi) throws TelegramApiException {
        this.botService = botService;
        name = config.getBotName();
        token = config.getToken();
        telegramBotsApi.registerBot(this);
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                Message message = update.getMessage();
                long chatId = message.getChatId();
                String receivedMessage = message.getText();
                String name = message.getChat().getUserName();
                Person person = botService.getOneByChatId(chatId);

                switch (receivedMessage) {
                    case "/start" -> sendMessage(chatId, botService.start(person, chatId, name));
                    case "/menu" -> sendMessage(chatId, botService.menu(person));
                    case "/addtarget" -> sendMessage(chatId, botService.beginAddTarget(person));
                    case "/mytargets" -> sendMessage(chatId, botService.myTargets(person));
                    case "/deletetarget" -> sendMessage(chatId, botService.deleteTargetBegin(person));
                    case "/info" -> sendMessage(chatId, "avito parser bot\nbot tracks the appearance of new posts from avito.ru for your targets");
                    default -> {
                        String lastCommand = person.getLastCommand();
                        if (lastCommand.equals("/addLink")) {
                            sendMessage(chatId, botService.addLink(person, receivedMessage));
                        } else if (lastCommand.contains("/addRule")) {
                            sendMessage(chatId, botService.addRule(person, receivedMessage));
                        } else if (lastCommand.equals("/deleteTarget")) {
                            sendMessage(chatId, botService.deleteTarget(person, receivedMessage));
                        } else {
                            sendMessage(chatId, "Команда не поддерживается");
                        }
                    }
                }
            }
        } catch (TelegramApiException e) {
            System.out.println("Ошибка отправки сообщения");
        }
    }

    public void sendMessage(long chatId, String response) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(response);
        execute(sendMessage);
    }

    @Override
    public String getBotUsername() {
        return name;
    }

    @Override
    public String getBotToken() {
        return token;
    }
}
