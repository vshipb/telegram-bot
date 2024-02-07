package io.qmbot.telegrambot.commands;

import io.qmbot.telegrambot.Bot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class AddChatFolder extends BotCommand {

    @Value("${bot.id}")
    private String masterIdString;
    @Value(Bot.BOT_CONFIG)
    private String config;
    @Autowired
    private Bot bot;

    public AddChatFolder() {
        super("AddBirthdaysFolder", "Add Birthdays Folder. Only for bot owner");
    }

    @Override
    public void execute(AbsSender absSender, Message message, String[] arguments) throws TelegramApiException, IOException {
        long userId = message.getFrom().getId();
        long masterId = Long.parseLong(masterIdString);
        if (userId == masterId) {
            Path directoryPath = Path.of(bot.chatsFolder + "/" + message.getChatId());
            Files.createDirectories(directoryPath);
            writeFile(directoryPath, "members");
            writeFile(directoryPath, "isBirthdayEnabled");
            writeFile(directoryPath, "isReactionsEnabled");
            writeFile(directoryPath, "isMeetNewMemberEnabled");
        } else {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(message.getChat().getId());
            sendMessage.setText("u r not owner sorry");
            absSender.execute(sendMessage);
        }
    }

    void writeFile(Path directoryPath, String fileName) throws IOException {
        Path filePath = Path.of(directoryPath + "/" + fileName + ".txt");
        if (!Files.exists(filePath)) {
            FileWriter fileWriter = new FileWriter(filePath.toString(), StandardCharsets.UTF_8);
            fileWriter.close();
        }
    }
}
