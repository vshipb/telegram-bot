package io.qmbot.telegrambot.commands;

import io.qmbot.telegrambot.Bot;
import java.io.File;
import java.util.Locale;
import org.apache.commons.io.FilenameUtils;
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

    public ShowReactionsCommand() {
        super("show_reactions", "Showing reactions");
    }

    private static String reactions(File[] files, AbsSender absSender, SendMessage message) throws TelegramApiException {
        if (files.length < 1) return "I have nothing to say to this";

        StringBuilder reactions = new StringBuilder();
        for (File file : files) {
            reactions.append(file.getName()).append("\n");
        }
        nameWithReaction(files, message, absSender);

        return reactions.toString();
    }

    private static void nameWithReaction(File[] files, SendMessage message, AbsSender absSender) throws TelegramApiException {
        for (File file : files) {
            message.setText(file.getName());

            String typeFile = FilenameUtils.getExtension(file.getName()).toLowerCase(Locale.ROOT);

            if (Bot.fileIsPhoto(typeFile)) {
                absSender.execute(SendPhoto.builder().chatId(message.getChatId()).photo(new InputFile(file))
                        .caption(file.getName()).build());
            } else if (Bot.fileIsAnimation(typeFile)) {
                absSender.execute(SendAnimation.builder().chatId(message.getChatId()).animation(new InputFile(file))
                        .caption(file.getName()).build());
            }

        }
    }

    @Override
    public void execute(AbsSender absSender, Message message, String[] arguments) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChat().getId());
        File[] dir = new File(Bot.CONFIG + Bot.newMemberFolder).listFiles();

        if (dir == null) return;

        String listOfReaction = reactions(dir, absSender, sendMessage);
        sendMessage.setText("My hello list: \n" + listOfReaction);


        absSender.execute(sendMessage);


        dir = new File(Bot.CONFIG + Bot.repliesFolder).listFiles();
        if (dir == null) return;

        for (File word : dir) {
            File[] files = word.listFiles();

            if (files == null) return;

            listOfReaction = reactions(files, absSender, sendMessage);
            sendMessage.setText("My list of reactions to " + word.getName() + ": \n" + listOfReaction);

            absSender.execute(sendMessage);

        }
    }
}
