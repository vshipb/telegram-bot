package io.qmbot.telegrambot;

import io.qmbot.telegrambot.commandbot.commands.BotCommand;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.games.Animation;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.io.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Comparator;
import java.util.List;

public class AddReactionCommand extends BotCommand {
    public AddReactionCommand() {
        super("add_reaction", "Add reaction");
    }

    @Override
    public void execute(AbsSender absSender, Message message, String[] strings) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChat().getId());
        sendMessage.setText("Reaction added");

        List<PhotoSize> photos = message.getReplyToMessage().getPhoto();
        Animation animation = message.getReplyToMessage().getAnimation();

        if (photos != null) {
            PhotoSize largestPhoto = photos.stream()
                    .max(Comparator.comparing(PhotoSize::getFileSize))
                    .orElse(null);

            final String fileId = largestPhoto.getFileId();
            final String fileName = largestPhoto.getFileUniqueId() + ".jpg";

            try {
                fileUpload(fileName, fileId, strings[0]);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (animation == null) return;

        final String fileId = animation.getFileId();
        final String fileName = animation.getFileUniqueId() + ".mp4";

        try {
            fileUpload(fileName, fileId, strings[0]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void fileUpload(String fileName, String fileId, String folder) throws IOException {
        URL url = new URL("https://api.telegram.org/bot" + Bot.BOT_TOKEN + "/getFile?file_id=" + fileId);

        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        String getFile = br.readLine();

        JSONObject jsonObject = new JSONObject(getFile);
        JSONObject path = jsonObject.getJSONObject("result");
        String file_path = path.getString("file_path");

        String directoryPath = Bot.CONFIG + "/reactions/replies/" + folder;
        File directory = new File(directoryPath);

        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Unable to create directory");
        }
        streamFile(folder, fileName, file_path);


    }

    private void streamFile(String folder, String fileName, String file_path) throws IOException {
        File file = new File(Bot.CONFIG + "/reactions/replies/" + folder + "/" + fileName);
        InputStream is = new URL("https://api.telegram.org/file/bot" + Bot.BOT_TOKEN + "/" + file_path).openStream();

        FileUtils.copyInputStreamToFile(is, file);
    }
}
