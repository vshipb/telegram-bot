package io.qmbot.telegrambot.commands;

import io.qmbot.telegrambot.Bot;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.games.Animation;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;

@Component
public class AddCongratsCommand extends BotCommand{
    @Value(Bot.BOT_TOKEN)
    private String botToken;
    @Value(Bot.BOT_CONFIG)
    private String config;

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

        String directoryPath = config + Bot.birthdaysFolder;
        File directory = new File(directoryPath);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Unable to create directory");
        }

        saveFile(directoryPath, fileName, filePath);

        absSender.execute(sendMessage);
    }

    private void saveFile(String folder, String fileName, String filePath) throws IOException {
        File file = new File(config + Bot.birthdaysFolder + "/" + fileName);
        InputStream is = new URL("https://api.telegram.org/file/bot" + botToken + "/" + filePath).openStream();

        FileUtils.copyInputStreamToFile(is, file);
    }
}
