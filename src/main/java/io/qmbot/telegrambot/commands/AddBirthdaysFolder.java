package io.qmbot.telegrambot.commands;

import io.qmbot.telegrambot.Bot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class AddBirthdaysFolder extends BotCommand {

    @Value("${bot.id}")
    private String masterIdString;
    @Value(Bot.BOT_CONFIG)
    private String config;

    public AddBirthdaysFolder() {
        super("AddBirthdaysFolder", "Add Birthdays Folder. Only for bot owner");
    }

    @Override
    public void execute(AbsSender absSender, Message message, String[] arguments) throws TelegramApiException, IOException {
        long userId = message.getFrom().getId();
        long masterId = Long.parseLong(masterIdString);
        if (userId == masterId) {
            String directoryPath = config + Bot.chatsFolder;
            File directory = new File(directoryPath);
            if (!directory.exists() && !directory.mkdirs()) {
                throw new IOException("Unable to create directory");
            }
            String filePath = directoryPath + "/" + message.getChatId() + ".txt";
            File file = new File(filePath);
            if (!file.exists()) {
                FileWriter fileWriter = new FileWriter(filePath, StandardCharsets.UTF_8);
                fileWriter.close();
            }
        } else {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(message.getChat().getId());
            sendMessage.setText("u r not owner sorry");
            absSender.execute(sendMessage);
        }
    }
}
