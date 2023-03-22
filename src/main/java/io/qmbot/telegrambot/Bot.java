package io.qmbot.telegrambot;

import java.io.File;
import java.util.Random;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Bot extends TelegramLongPollingBot {
    private static final String BOT_TOKEN = System.getProperty("bot.token");
    private static final String CONFIG = System.getProperty("bot.config");
    private static final String BOT_NAME = System.getProperty("bot.name");
    private static final Logger logger = LoggerFactory.getLogger(Bot.class);

    public static void main(String[] args) throws TelegramApiException {
        System.out.println("Token: " + BOT_TOKEN);
        Bot bot = new Bot(new DefaultBotOptions(), BOT_TOKEN);
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(bot);
    }

    @Override
    public void onUpdateReceived(Update update) {
        logger.info("Update: {}", update);
        if (update.getMessage().getNewChatMembers().size() > 0) {
            reaction(new File(CONFIG + "/reactions/newMember").listFiles(), update.getMessage());
        }

        File[] arrDirs = new File(CONFIG + "/reactions/replies").listFiles();
        if (arrDirs == null) return;
        for (File dir : arrDirs) {
            if (update.getMessage().getText().toLowerCase().contains(dir.getName())) {
                reaction(new File(CONFIG + "/reactions/replies/" + dir.getName()).listFiles(),
                        update.getMessage());
            }
        }
    }

    public void reaction(File[] files, Message message) {
        if (files == null) return;
        Random random = new Random();
        File file = files[random.nextInt(files.length)];
        String typeFile = FilenameUtils.getExtension(file.getName());
        try {
            if (typeFile.equals("png") || typeFile.equals("jpg")) {
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

    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }

    protected Bot(DefaultBotOptions options, String token) {
        super(options, token);
    }

}
