package io.qmbot.telegrambot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.objects.Message;

public class FeedbackCommand extends BotCommand {
    private boolean waitingForFeedback = false;

    public FeedbackCommand() {
        super("feedback", "Provide feedback or report issues with the bot");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        SendMessage message = new SendMessage();
        message.setChatId(chat.getId());
        message.setText("Describe the problem. The message will be sent to the owner of the bot.");

        boolean waitingForFeedback = true;

        try {
            absSender.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }


    }

    public void processMessage(AbsSender absSender, Message message) {
        if (waitingForFeedback) {
            SendMessage reply = new SendMessage();
            reply.setChatId(message.getChatId());
            reply.setText("Thank you for your feedback!");

            try {
                absSender.execute(reply);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            waitingForFeedback = false;
        }
    }
}
