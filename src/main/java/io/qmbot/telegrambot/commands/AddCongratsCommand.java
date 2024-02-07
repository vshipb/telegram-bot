package io.qmbot.telegrambot.commands;

import io.qmbot.telegrambot.Bot;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.games.Animation;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class AddCongratsCommand extends BotCommand {
    @Value(Bot.BOT_TOKEN)
    private String botToken;
    @Value(Bot.BOT_CONFIG)
    private String config;
    @Autowired
    private Bot bot;


    public AddCongratsCommand() {
        super("add_congrats", "Add congrats");
    }

    @Override
    public void execute(AbsSender absSender, Message message, String[] arguments) throws TelegramApiException, IOException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChat().getId());
        sendMessage.setText("Congrats added");
        if (message.getReplyToMessage() == null) return;
        Animation animation = message.getReplyToMessage().getAnimation();
        if (animation == null) return;
        String fileId = animation.getFileId();
        String fileName = animation.getFileUniqueId() + ".mp4";
        fileUpload(fileName, fileId, absSender, sendMessage);
    }

    private void fileUpload(String fileName, String fileId, AbsSender absSender, SendMessage sendMessage)
            throws IOException, TelegramApiException {
        URL url = new URL("https://api.telegram.org/bot" + botToken + "/getFile?file_id=" + fileId);
        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
        String getFile = br.readLine();
        JSONObject jsonObject = new JSONObject(getFile);
        JSONObject path = jsonObject.getJSONObject("result");
        String filePath = path.getString("file_path");
        Path directoryPath = bot.birthdaysFolder;
        Files.createDirectories(directoryPath);
        saveFile(fileName, filePath);
        absSender.execute(sendMessage);
    }

    private void saveFile(String fileName, String filePath) throws IOException {
        Path path = bot.birthdaysFolder.resolve(fileName);
        InputStream is = new URL("https://api.telegram.org/file/bot" + botToken + "/" + filePath).openStream();
        try (OutputStream os = Files.newOutputStream(path)) {
            IOUtils.copy(is, os);
        }
    }
}
