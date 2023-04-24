package io.qmbot.telegrambot;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qmbot.telegrambot.commands.AddReactionCommand;
import io.qmbot.telegrambot.commands.FeedbackCommand;
import io.qmbot.telegrambot.commands.HelpCommand;
import io.qmbot.telegrambot.commands.ShowReactionsCommand;
import io.qmbot.telegrambot.commands.StartCommand;
import java.io.File;
import java.util.Locale;
import java.util.Random;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class Bot extends TelegramLongPollingCommandBot {
    public static final String BOT_TOKEN = System.getProperty("bot.token");
    public static final String CONFIG = System.getProperty("bot.config");
    private static final String BOT_NAME = System.getProperty("bot.name");
    public static final String MASTER_ID = System.getProperty("bot.id");
    private static final Random random = new Random();
    public static final String newMemberFolder = "/reactions/newMember";
    public static final String repliesFolder = "/reactions/replies";
    public static final String failedToExecute = "Failed to execute";
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(Bot.class);

    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }

    public String getBotToken() {
        return BOT_TOKEN;
    }

    private void reaction(File[] files, Message message) {
        if (files == null) return;

        File file = files[random.nextInt(files.length)];
        String typeFile = FilenameUtils.getExtension(file.getName()).toLowerCase(Locale.ROOT);
        try {
            if (fileIsPhoto(typeFile)) {
                execute(SendPhoto.builder().chatId(message.getChatId()).replyToMessageId(message.getMessageId())
                        .photo(new InputFile(file)).build());
            } else if (fileIsAnimation(typeFile)) {
                execute(SendAnimation.builder().chatId(message.getChatId()).replyToMessageId(message.getMessageId())
                        .animation(new InputFile(file)).build());
            }
        } catch (TelegramApiException e) {
            logger.error(failedToExecute, e);
        }
    }
    @Autowired
    Bot(DefaultBotOptions options, StartCommand startCommand, HelpCommand command) {
        super(options);

        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        register(startCommand);
        register(command);
        register(new FeedbackCommand());
        register(new ShowReactionsCommand());
        register(new AddReactionCommand());
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        logger.info("Update: {}", objectToString(update));
        Message message = update.getMessage();

        if (message == null) return;

        if (!message.getNewChatMembers().isEmpty()) {
            reaction(new File(CONFIG + newMemberFolder).listFiles(), update.getMessage());
        }

        String text = message.getText();

        if (text == null) return;

        File[] arrDirs = new File(CONFIG + repliesFolder).listFiles();
        if (arrDirs == null) return;
        for (File dir : arrDirs) {
            if (text.toLowerCase(Locale.ROOT).contains(dir.getName())) {
                reaction(new File(CONFIG + repliesFolder + "/" + dir.getName()).listFiles(), message);
            }
        }
    }

    private static String objectToString(Object o) {
        try {
            return mapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static boolean fileIsPhoto(String typeFile) {
        return typeFile.equals("png") || typeFile.equals("jpg") || typeFile.equals("jpeg");

    }

    public static boolean fileIsAnimation(String typeFile) {
        return typeFile.equals("mp4") || typeFile.equals("gif");
    }

}
