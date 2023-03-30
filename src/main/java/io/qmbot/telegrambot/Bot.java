package io.qmbot.telegrambot;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.commands.DeleteMyCommands;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeChat;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Bot extends TelegramLongPollingCommandBot {
    public static final String BOT_TOKEN = System.getProperty("bot.token");
    public static final String CONFIG = System.getProperty("bot.config");
    public static final String BOT_NAME = System.getProperty("bot.name");
    public static final Logger logger = LoggerFactory.getLogger(Bot.class);

    public static final String MASTER_ID = System.getProperty("bot.masterId");

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
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        sendMessage.setText(message.getChatId().toString());
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public Bot(DefaultBotOptions options) {
        super(options);

        register(new StartCommand());
        register(new HelpCommand());
        register(new FeedbackCommand());
        register(new ShowReactionsCommand());
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        logger.info("Update: {}", update);

        if (update.getMessage() == null) return;

        if (update.getMessage().getNewChatMembers().size() > 0) {
            reaction(new File(CONFIG + "/reactions/newMember").listFiles(), update.getMessage());
        }

        if (update.getMessage().getText() == null) return;

        File[] arrDirs = new File(CONFIG + "/reactions/replies").listFiles();
        if (arrDirs == null) return;
        for (File dir : arrDirs) {
            if (update.getMessage().getText().toLowerCase().contains(dir.getName())) {
                reaction(new File(CONFIG + "/reactions/replies/" + dir.getName()).listFiles(),
                        update.getMessage());
            }
        }

//        if (update.getMessage().isCommand())) {
//
//        }
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
