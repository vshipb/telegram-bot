package io.qmbot.telegrambot.commands;

import io.qmbot.telegrambot.Bot;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.games.Animation;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class AddReactionCommand extends BotCommand {
    public AddReactionCommand() {
        super("add_reaction", "Add reaction");
    }

    @Override
    public void execute(AbsSender absSender, Message message, String[] arguments) throws TelegramApiException, IOException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChat().getId());
        sendMessage.setText("Reaction added");

        if (message.getReplyToMessage() == null) return;

        List<PhotoSize> photos = message.getReplyToMessage().getPhoto();
        Animation animation = message.getReplyToMessage().getAnimation();

        if (photos != null) {
            PhotoSize largestPhoto = photos.stream()
                    .max(Comparator.comparing(PhotoSize::getFileSize))
                    .orElse(null);

            if (largestPhoto == null) return;

            String fileId = largestPhoto.getFileId();
            String fileName = largestPhoto.getFileUniqueId() + ".jpg";

            fileUpload(fileName, fileId, arguments[0], absSender, sendMessage);

        }

        if (animation == null) return;

        String fileId = animation.getFileId();
        String fileName = animation.getFileUniqueId() + ".mp4";

        fileUpload(fileName, fileId, arguments[0], absSender, sendMessage);

    }

    private static void fileUpload(String fileName, String fileId, String folder, AbsSender absSender, SendMessage sendMessage)
            throws IOException, TelegramApiException {

        URL url = new URL("https://api.telegram.org/bot" + Bot.BOT_TOKEN + "/getFile?file_id=" + fileId);

        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
        String getFile = br.readLine();

        JSONObject jsonObject = new JSONObject(getFile);
        JSONObject path = jsonObject.getJSONObject("result");
        String filePath = path.getString("file_path");

        String directoryPath = Bot.CONFIG + Bot.repliesFolder + "/" + folder;
        File directory = new File(directoryPath);

        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Unable to create directory");
        }
        saveFile(folder, fileName, filePath);

        absSender.execute(sendMessage);
    }

    private static void saveFile(String folder, String fileName, String filePath) throws IOException {
        File file = new File(Bot.CONFIG + Bot.repliesFolder + "/" + folder + "/" + fileName);
        InputStream is = new URL("https://api.telegram.org/file/bot" + Bot.BOT_TOKEN + "/" + filePath).openStream();

        FileUtils.copyInputStreamToFile(is, file);
    }
}
