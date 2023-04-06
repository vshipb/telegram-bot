package io.qmbot.telegrambot;

import java.io.File;
import java.util.Random;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Bot extends TelegramLongPollingCommandBot {
    public static final String BOT_TOKEN = System.getProperty("bot.token");
    public static final String CONFIG = System.getProperty("bot.config");
    public static final String BOT_NAME = System.getProperty("bot.name");
    public static final Logger logger = LoggerFactory.getLogger(Bot.class);

    public static void main(String[] args) throws TelegramApiException {
        System.out.println("Token: " + BOT_TOKEN);
        Bot bot = new Bot(new DefaultBotOptions());
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(bot);
    }

    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }

    public String getBotToken() {
        return BOT_TOKEN;
    }

    @Override
    public void onRegister() {
        super.onRegister();
    }

    public void reaction(File[] files, Message message) {
        if (files == null) return;
        Random random = new Random();
        File file = files[random.nextInt(files.length)];
        String typeFile = FilenameUtils.getExtension(file.getName()).toLowerCase();
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

    public Bot(DefaultBotOptions options) {
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

        if (message.getNewChatMembers().size() > 0) {
            reaction(new File(CONFIG + "/reactions/newMember").listFiles(), update.getMessage());
        }

        String text = message.getText();

        if (text == null) return;

        File[] arrDirs = new File(CONFIG + "/reactions/replies").listFiles();
        if (arrDirs == null) return;
        for (File dir : arrDirs) {
            if (text.toLowerCase().contains(dir.getName())) {
                reaction(new File(CONFIG + "/reactions/replies/" + dir.getName()).listFiles(), message);
            }
        }
    }


    @Override
    public void processInvalidCommandUpdate(Update update) {
        super.processInvalidCommandUpdate(update);
    }

    @Override
    public boolean filter(Message message) {
        return super.filter(message);
    }
}
