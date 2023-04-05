package io.qmbot.telegrambot;

import io.qmbot.telegrambot.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.objects.Message;

public class FeedbackCommand extends BotCommand {

    public FeedbackCommand() {
        super("feedback", "Provide feedback or report issues with the bot");
    }


    @Override
    public void execute(AbsSender absSender, Message message, String[] arguments) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChat().getId());
        sendMessage.setText("Describe the problem. The message will be sent to the owner of the bot.");

        try {
            absSender.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }
}
