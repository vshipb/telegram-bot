package io.qmbot.telegrambot.commands;

import io.qmbot.telegrambot.Bot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class ShowReactionsCommand extends BotCommand {
    @Value(Bot.BOT_CONFIG)
    private String config;
    @Autowired
    private Bot bot;

    public ShowReactionsCommand() {
        super("show_reactions", "Showing reactions");
    }

    private static String reactions(List<Path> paths, AbsSender absSender, SendMessage message) throws TelegramApiException {
        if (paths.size() < 1) return "I have nothing to say to this";
        StringBuilder reactions = new StringBuilder();
        for (Path path : paths) {
            reactions.append(path.getFileName().toString()).append("\n");
        }
        nameWithReaction(paths, message, absSender);
        return reactions.toString();
    }

    private static void nameWithReaction(List<Path> paths, SendMessage message, AbsSender absSender) throws TelegramApiException {
        for (Path path : paths) {
            String name = path.getFileName().toString();
            message.setText(name);
            String typeFile = FilenameUtils.getExtension(name).toLowerCase(Locale.ROOT);
            if (Bot.isPhoto(typeFile)) {
                absSender.execute(SendPhoto.builder().chatId(message.getChatId()).photo(new InputFile(path.toFile()))
                        .caption(name).build());
            } else if (Bot.isAnimation(typeFile)) {
                absSender.execute(SendAnimation.builder().chatId(message.getChatId()).animation(new InputFile(path.toFile()))
                        .caption(name).build());
            }
        }
    }

    @Override
    public void execute(AbsSender absSender, Message message, String[] arguments) throws TelegramApiException, IOException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChat().getId());
        List<Path> dir = Files.list(bot.newMemberFolder).toList();
        if (dir.isEmpty()) return;
        String listOfReaction = reactions(dir, absSender, sendMessage);
        sendMessage.setText("My hello list: \n" + listOfReaction);
        absSender.execute(sendMessage);
        List<Path> paths = Files.list(bot.repliesFolder).toList();
        if (paths.isEmpty()) return;
        for (Path word : paths) {
            List<Path> files = Files.list(word).toList();
            if (files.isEmpty()) return;
            listOfReaction = reactions(files, absSender, sendMessage);
            sendMessage.setText("My list of reactions to " + word.getFileName().toString() + ": \n" + listOfReaction);
            absSender.execute(sendMessage);
        }
    }
}
