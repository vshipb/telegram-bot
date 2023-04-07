package io.qmbot.telegrambot;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.games.Animation;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class AddReactionCommand extends BotCommand {
    public AddReactionCommand() {
        super("add_reaction", "Add reaction");
    }

    @Override
    public void execute(AbsSender absSender, Message message, String[] strings) throws TelegramApiException, IOException {
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

            final String fileId = largestPhoto.getFileId();
            final String fileName = largestPhoto.getFileUniqueId() + ".jpg";

            fileUpload(fileName, fileId, strings[0], absSender, sendMessage);

        }

        if (animation == null) return;

        final String fileId = animation.getFileId();
        final String fileName = animation.getFileUniqueId() + ".mp4";

        fileUpload(fileName, fileId, strings[0], absSender, sendMessage);

    }

    public void fileUpload(String fileName, String fileId, String folder, AbsSender absSender, SendMessage sendMessage)
            throws IOException, TelegramApiException {

        URL url = new URL("https://api.telegram.org/bot" + Bot.BOT_TOKEN + "/getFile?file_id=" + fileId);

        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        String getFile = br.readLine();

        JSONObject jsonObject = new JSONObject(getFile);
        JSONObject path = jsonObject.getJSONObject("result");
        String filePath = path.getString("file_path");

        String directoryPath = Bot.CONFIG + "/reactions/replies/" + folder;
        File directory = new File(directoryPath);

        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Unable to create directory");
        }
        streamFile(folder, fileName, filePath);
        absSender.execute(sendMessage);
    }

    private void streamFile(String folder, String fileName, String filePath) throws IOException {
        File file = new File(Bot.CONFIG + "/reactions/replies/" + folder + "/" + fileName);
        InputStream is = new URL("https://api.telegram.org/file/bot" + Bot.BOT_TOKEN + "/" + filePath).openStream();

        FileUtils.copyInputStreamToFile(is, file);
    }
}
