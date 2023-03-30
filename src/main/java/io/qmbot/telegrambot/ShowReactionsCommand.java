package io.qmbot.telegrambot;

import org.apache.commons.io.FilenameUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import java.io.File;
public class ShowReactionsCommand extends BotCommand {

    public ShowReactionsCommand() {
        super("show_reactions", "Showing reactions");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        SendMessage message = new SendMessage();
        message.setChatId(chat.getId());
        File[] dir = new File(Bot.CONFIG + "/reactions/newMember").listFiles();
        String listOfReaction = reactions(dir, absSender, message);
        message.setText("Мой список приветсвий: \n" + listOfReaction);

        try {
            absSender.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        dir = new File(Bot.CONFIG + "/reactions/replies").listFiles();
        for (File word : dir) {
            File[] files = word.listFiles();
            listOfReaction = reactions(files,absSender,message);
            message.setText("Мой список реакций на " + word.getName() + ": \n" + listOfReaction);
            try {
                absSender.execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    public String reactions(File[] files, AbsSender absSender, SendMessage message) {
        if (files == null) return "На это Аде нечего сказать";

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
                if (typeFile.equals("png") || typeFile.equals("jpg")) {
                    absSender.execute(SendPhoto.builder().chatId(message.getChatId()).photo(new InputFile(file)).photo(new InputFile(file))
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
}
