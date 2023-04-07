package io.qmbot.telegrambot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class FeedbackCommand extends BotCommand {

    public FeedbackCommand() {
        super("feedback", "Provide feedback or report issues with the bot");
    }


    @Override
    public void execute(AbsSender absSender, Message message, String[] arguments) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(Bot.MASTER_ID);
        sendMessage.setText(message.getReplyToMessage().getText());

        sendMessage(absSender, sendMessage);

        sendMessage.setChatId(message.getChatId());
        sendMessage.setText("Describe the problem. The message will be sent to the owner of the bot.");

        sendMessage(absSender, sendMessage);
    }

    private void sendMessage(AbsSender absSender, SendMessage sendMessage) throws TelegramApiException {
        absSender.execute(sendMessage);
    }
}
