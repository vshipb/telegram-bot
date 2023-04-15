package io.qmbot.telegrambot;

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
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Bot extends TelegramLongPollingCommandBot {
    public static final String BOT_TOKEN = System.getProperty("bot.token");
    public static final String CONFIG = System.getProperty("bot.config");
    private static final String BOT_NAME = System.getProperty("bot.name");
    public static final String MASTER_ID = System.getProperty("bot.id");
    private static final Random random = new Random();
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
            if (typeFile.equals("png") || typeFile.equals("jpg") || typeFile.equals("jpeg")) {
                execute(SendPhoto.builder().chatId(message.getChatId()).replyToMessageId(message.getMessageId())
                        .photo(new InputFile(file)).build());
            } else if (typeFile.equals("mp4") || typeFile.equals("gif")) {
                execute(SendAnimation.builder().chatId(message.getChatId()).replyToMessageId(message.getMessageId())
                        .animation(new InputFile(file)).build());
            }
        } catch (TelegramApiException e) {
            logger.error("Failed to execute", e);
        }
    }

    Bot(DefaultBotOptions options) {
        super(options);

        register(new StartCommand());
        register(new HelpCommand());
        register(new FeedbackCommand());
        register(new ShowReactionsCommand());
        register(new AddReactionCommand());
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        logger.info("Update: {}", update);
        Message message = update.getMessage();

        if (message == null) return;

        if (!message.getNewChatMembers().isEmpty()) {
            reaction(new File(CONFIG + "/reactions/newMember").listFiles(), update.getMessage());
        }

        String text = message.getText();

        if (text == null) return;

        File[] arrDirs = new File(CONFIG + "/reactions/replies").listFiles();
        if (arrDirs == null) return;
        for (File dir : arrDirs) {
            if (text.toLowerCase(Locale.ROOT).contains(dir.getName())) {
                reaction(new File(CONFIG + "/reactions/replies/" + dir.getName()).listFiles(), message);
            }
        }
    }
}
