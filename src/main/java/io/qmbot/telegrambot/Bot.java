package io.qmbot.telegrambot;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qmbot.telegrambot.commands.BotCommand;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
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

@Component
public class Bot extends TelegramLongPollingCommandBot implements InitializingBean {
    private static final Pattern COMPILE = Pattern.compile("\\R");
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    public static final String BOT_TOKEN = "${bot.token}";
    public static final String BOT_CONFIG = "${bot.config}";
    @Value(BOT_TOKEN)
    private String botToken;
    @Value(BOT_CONFIG)
    private String config;
    @Value("${bot.name}")
    private String botName;
    @Autowired
    private ChatConfiguration chatConfiguration;
    private static final Random random = new Random();
    public final Path chatsFolder = Path.of(config, "chats");
    private final Path reactions = Path.of(config, "reactions");
    public final Path birthdaysFolder = Path.of(String.valueOf(reactions), "birthday");
    public final Path newMemberFolder = Path.of(String.valueOf(reactions), "newMember");
    public final Path repliesFolder = Path.of(String.valueOf(reactions), "replies");
    public static final String failedToExecute = "Failed to execute";
    static final String membersFile = "members.txt";
    private final List<Path> newMemberReactions;
    private final List<Path> birthdaysReactions;
    private final List<Path> chats;
    private final Map<String, List<Path>> wordsReactions;
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(Bot.class);

    @Autowired
    Bot(List<BotCommand> commands) throws IOException {
        super(new DefaultBotOptions());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        for (BotCommand cmd : commands) {
            register(cmd);
        }
        try (Stream<Path> stream = Files.list(chatsFolder)) {
            chats = stream.toList();
        }
        try (Stream<Path> stream = Files.list(newMemberFolder)) {
            newMemberReactions = stream.toList();
        }
        List<Path> replies;
        try (Stream<Path> stream = Files.list(repliesFolder)) {
            replies = stream.toList();
        }
        try (Stream<Path> stream = Files.list(birthdaysFolder)) {
            birthdaysReactions = stream.toList();
        }
        wordsReactions = new HashMap<>();
        for (Path word : replies) {
            try (Stream<Path> stream = Files.list(word)) {
                wordsReactions.put(word.getFileName().toString(), stream.toList());
            }
        }
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    public String getBotToken() {
        return botToken;
    }

    private void reactionToMessage(List<Path> files, Message message) {
        if (files == null) return;
        Long chatId = message.getChatId();
        Integer messageId = message.getMessageId();
        Path path = files.get(random.nextInt(files.size()));
        String typeFile = FilenameUtils.getExtension(path.getFileName().toString()).toLowerCase(Locale.ROOT);
        try {
            if (isPhoto(typeFile)) {
                execute(SendPhoto.builder().chatId(chatId).replyToMessageId(messageId).photo(new InputFile(path.toFile())).build());
            } else if (isAnimation(typeFile)) {
                execute(SendAnimation.builder().chatId(chatId).replyToMessageId(messageId).animation(new InputFile(path.toFile()))
                        .build());
            }
        } catch (TelegramApiException e) {
            logger.error(failedToExecute, e);
        }
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        logger.info("Update: {}", objectToString(update));
        Message message = update.getMessage();
        if (message == null) return;
        long chatId = message.getChatId();
        if (chatConfiguration.isMeetNewMemberEnabled(chatId) && !message.getNewChatMembers().isEmpty()) {
            reactionToMessage(newMemberReactions, update.getMessage());
        }
        String text = message.getText();
        if (text == null) return;
        if (chatConfiguration.isReactionsEnabled(chatId)) {
            if (wordsReactions.isEmpty()) return;
            for (Map.Entry<String, List<Path>> entry : wordsReactions.entrySet()) {
                if (text.toLowerCase(Locale.ROOT).contains(entry.getKey())) {
                    reactionToMessage(entry.getValue(), message);
                }
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

    List<Path> getBirthdaysReactions() {
        return birthdaysReactions;
    }

    List<Path> getChats() {
        return chats;
    }
}
