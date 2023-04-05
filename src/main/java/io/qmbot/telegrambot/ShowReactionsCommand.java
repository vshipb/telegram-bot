package io.qmbot.telegrambot;

import io.qmbot.telegrambot.commandbot.commands.BotCommand;
import org.apache.commons.io.FilenameUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.io.File;
public class ShowReactionsCommand extends BotCommand {

    public ShowReactionsCommand() {
        super("show_reactions", "Showing reactions");
    }

    public String reactions(File[] files, AbsSender absSender, SendMessage message) {
        if (files.length < 1) return "I have nothing to say to this";

        StringBuilder reactions = new StringBuilder();
        for (File file : files) {
            reactions.append(file.getName()).append("\n");
        }
          nameWithReaction(files, message, absSender);

        return reactions.toString();
    }

    public void nameWithReaction(File[] files, SendMessage message, AbsSender absSender) {
        for (File file : files) {
            message.setText(file.getName());

            String typeFile = FilenameUtils.getExtension(file.getName());

            try {
                if (typeFile.equals("png") || typeFile.equals("jpg") || typeFile.equals("JPEG")) {
                    absSender.execute(SendPhoto.builder().chatId(message.getChatId()).photo(new InputFile(file))
                            .caption(file.getName()).build());
                } else if (typeFile.equals("mp4") || typeFile.equals("gif")) {
                    absSender.execute(SendAnimation.builder().chatId(message.getChatId()).animation(new InputFile(file))
                            .caption(file.getName()).build());
                }
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void execute(AbsSender absSender, Message message, String[] arguments) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChat().getId());
        File[] dir = new File(Bot.CONFIG + "/reactions/newMember").listFiles();

        if (dir == null) return;

        String listOfReaction = reactions(dir, absSender, sendMessage);
        sendMessage.setText("My hello list: \n" + listOfReaction);

        try {
            absSender.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        dir = new File(Bot.CONFIG + "/reactions/replies").listFiles();
        if (dir == null) return;

        for (File word : dir) {
            File[] files = word.listFiles();

            if (files == null) return;

            listOfReaction = reactions(files,absSender,sendMessage);
            sendMessage.setText("My list of reactions to " + word.getName() + ": \n" + listOfReaction);
            try {
                absSender.execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
}
