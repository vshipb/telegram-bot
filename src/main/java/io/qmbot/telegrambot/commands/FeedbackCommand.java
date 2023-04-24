package io.qmbot.telegrambot.commands;

import io.qmbot.telegrambot.Bot;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
@Component
public class FeedbackCommand extends BotCommand {

    public FeedbackCommand() {
        super("feedback", "Provide feedback or report issues with the bot");
    }


    @Override
    public void execute(AbsSender absSender, Message message, String[] arguments) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(Bot.MASTER_ID);
        sendMessage.setText(message.getReplyToMessage().getText());

        absSender.execute(sendMessage);

        sendMessage.setChatId(message.getChatId());
        sendMessage.setText("Describe the problem. The message will be sent to the owner of the bot.");

        absSender.execute(sendMessage);
    }
}
