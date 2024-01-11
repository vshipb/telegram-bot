package io.qmbot.telegrambot;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qmbot.telegrambot.commands.BotCommand;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.extensions.bots.commandbot.CommandBot;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
public class Bot extends TelegramLongPollingCommandBot implements InitializingBean {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    public static final String BOT_TOKEN = "${bot.token}";
    public static final String BOT_CONFIG = "${bot.config}";
    @Value(BOT_TOKEN)
    private String botToken;
    @Value(BOT_CONFIG)
    private String config;
    @Value("${bot.name}")
    private String botName;

    private static final Random random = new Random();
    public static final String birthdaysFolder = "/reactions/birthday";
    public static final String chatsFolder = "/chats";
    public static final String newMemberFolder = "/reactions/newMember";
    public static final String repliesFolder = "/reactions/replies";
    public static final String failedToExecute = "Failed to execute";
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(Bot.class);

    @Autowired
    Bot(List<BotCommand> commands) {
        super(new DefaultBotOptions());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        for (BotCommand cmd : commands) {
            register(cmd);
        }
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    public String getBotToken() {
        return botToken;
    }

    private void reactionToMessage(File[] files, Message message) {
        if (files == null) return;
        Long chatId = message.getChatId();
        Integer messageId = message.getMessageId();

        File file = files[random.nextInt(files.length)];
        String typeFile = FilenameUtils.getExtension(file.getName()).toLowerCase(Locale.ROOT);
        try {
            if (isPhoto(typeFile)) {
                execute(SendPhoto.builder().chatId(chatId).replyToMessageId(messageId).photo(new InputFile(file)).build());
            } else if (isAnimation(typeFile)) {
                execute(SendAnimation.builder().chatId(chatId).replyToMessageId(messageId).animation(new InputFile(file))
                        .build());
            }
        } catch (TelegramApiException e) {
            logger.error(failedToExecute, e);
        }
    }


    private void congrats(File[] files, String chatId, String user) {
        if (files == null) return;

        File file = files[random.nextInt(files.length)];
        String typeFile = FilenameUtils.getExtension(file.getName()).toLowerCase(Locale.ROOT);
        try {
            if (isPhoto(typeFile)) {
                execute(SendPhoto.builder().chatId(chatId).photo(new InputFile(file)).build());
            } else if (isAnimation(typeFile)) {
                execute(SendAnimation.builder().chatId(chatId).animation(new InputFile(file)).build());
            }
        } catch (TelegramApiException e) {
            logger.error(failedToExecute, e);
        }

        SendMessage sendMessage = new SendMessage(chatId, "Happy Birthday, @" + user + "!");
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private Map<String, String> users(File input) throws IOException {
        FileReader fileReader = new FileReader(input);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String[] splitString = bufferedReader.readLine().split("\n");
        bufferedReader.close();
        fileReader.close();
        Map<String, String> users = new HashMap<>();
        for (String user : splitString) {
            String[] data = user.split(" ");
            users.put(data[0], data[1]);
        }
        return users;
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        logger.info("Update: {}", objectToString(update));
        Message message = update.getMessage();

        if (message == null) return;

        if (!message.getNewChatMembers().isEmpty()) {
            reactionToMessage(new File(config + newMemberFolder).listFiles(), update.getMessage());
        }

        String text = message.getText();

        if (text == null) return;

        File[] arrDirs = new File(config + repliesFolder).listFiles();
        if (arrDirs == null) return;
        for (File dir : arrDirs) {
            if (text.toLowerCase(Locale.ROOT).contains(dir.getName())) {
                reactionToMessage(new File(config + repliesFolder + "/" + dir.getName()).listFiles(), message);
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

    public static boolean isPhoto(String typeFile) {
        return typeFile.equals("png") || typeFile.equals("jpg") || typeFile.equals("jpeg");

    }

    public static boolean isAnimation(String typeFile) {
        return typeFile.equals("mp4") || typeFile.equals("gif");
    }

    @Override
    public void afterPropertiesSet() throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(this);
    }
}
