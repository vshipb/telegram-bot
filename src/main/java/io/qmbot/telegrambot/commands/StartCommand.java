package io.qmbot.telegrambot.commands;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class StartCommand extends BotCommand {
    public StartCommand() {
        super("start", "Start using the bot");
    }

    @Override
    public void execute(AbsSender absSender, Message message, String[] arguments) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChat().getId());
        sendMessage.setText("Hi, I'm Ada Wong. Need /help ?");

        absSender.execute(sendMessage);
    }
}