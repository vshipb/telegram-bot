package io.qmbot.telegrambot;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.qmbot.telegrambot.Bot.*;

@Component
public class ScheduledTasks {
    private static final Pattern COMPILE = Pattern.compile("\\R");

    @Autowired
    public ScheduledTasks(@Value(BOT_CONFIG) String config, Bot bot) {
        this.config = config;
        this.bot = bot;
    }

    @Value("${targetTime}")
    private String targetTimeStr;

    //@Value(BOT_CONFIG)
    private final String config;

    // @Autowired
    private final Bot bot;

    private static final Random random = new Random();
    private static final String chatsFolder = "/chats";
    private static final String birthdaysFolder = "/reactions/birthday";
    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Scheduled(fixedRate = 1000 * 60 * 60 * 24, initialDelayString="#{initialDelayCalculate}")
    public void reportCurrentTime() throws IOException {
        log.info("The time is now {}", dateFormat.format(new Date()));
        checkCalendar();
    }

    @Bean
    public long initialDelayCalculate(){
        long l = parse(targetTimeStr);
        l = l - System.currentTimeMillis();
        return l;
    }

    public static long parse(String source) {
        try {
            return dateFormat.parse(source).getTime();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkCalendar() throws IOException {
        //String pattern = "yyyy-MM-dd";
        LocalDate currentDate = LocalDate.now();
        String currentDateString = currentDate.toString().substring(5);
        File[] chats = new File(config + chatsFolder).listFiles();
        for (File chat : chats) {
            String chatId = chat.getName().substring(0, chat.getName().length() - 4);
            Map<String, String> users = users(Path.of(config + chatsFolder + "/" + chatId + ".txt"));
            for (String user : users.keySet()) {
                String birthdayDateString = users.get(user);
                if (currentDateString.equals(birthdayDateString)) {
                    congrats(new File(config + birthdaysFolder).listFiles(), chatId, user);
                }
            }
        }
    }

    private void congrats(File[] files, String chatId, String user) {
        if (files == null) return;

        File file = files[random.nextInt(files.length)];
        String typeFile = FilenameUtils.getExtension(file.getName()).toLowerCase(Locale.ROOT);
        try {
            if (isPhoto(typeFile)) {
                bot.execute(SendPhoto.builder().chatId(chatId).photo(new InputFile(file)).build());
            } else if (isAnimation(typeFile)) {
                bot.execute(SendAnimation.builder().chatId(chatId).animation(new InputFile(file)).build());
            }
        } catch (TelegramApiException e) {
            log.error(failedToExecute, e);
        }

        SendMessage sendMessage = new SendMessage(chatId, "Happy Birthday, @" + user + "!");
        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private Map<String, String> users(Path path) throws IOException {
        try (Stream<String> lines = Files.lines(path)) {
            return lines.map(line -> line.split(" ", 2))
                    .collect(Collectors.toMap(arr -> arr[0], arr -> arr[1].trim()));
        }
    }
}
