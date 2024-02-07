package io.qmbot.telegrambot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
class ChatConfiguration {
    @Autowired
    private Bot bot;
     boolean isBirthdayEnabled(long chatId) {
        Path path = Path.of(String.valueOf(bot.chatsFolder), String.valueOf(chatId) , "isBirthdayEnabled.txt");
        return Files.exists(path);
    }
    boolean isReactionsEnabled(long chatId) {
        Path path = Path.of(String.valueOf(bot.chatsFolder), String.valueOf(chatId) , "isReactionsEnabled.txt");
        return Files.exists(path);
    }
    boolean isMeetNewMemberEnabled(long chatId) {
        return Files.exists(Path.of(String.valueOf(bot.chatsFolder), String.valueOf(chatId), "isMeetNewMemberEnabled.txt"));
    }
}

//    void enableBirthday(long chatId) {
//
//    }
//    void disableBirthday(long chatId) {
//
//    }
//    void enableReactions(long chatId) {
//
//    }
//    void disableReactions(long chatId) {
//
//    }
