package io.qmbot.telegrambot.commands;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class HelpCommand extends BotCommand {

    private final List<BotCommand> commands;

    @Autowired
    public HelpCommand(List<BotCommand> commands) {
        super("help", "Test");
        this.commands = commands;
    }

    @Override
    public void execute(AbsSender absSender, Message message, String[] arguments) throws TelegramApiException {
        StringBuilder stringBuilder =  new StringBuilder();
        stringBuilder.append("Available commands:\n");
        for (BotCommand cmd : commands) {
            stringBuilder.append("/").append(cmd.getCommandIdentifier()).append(" - ").append(cmd.getDescription()).append("\n");
        }
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChat().getId());
        sendMessage.setText(stringBuilder.toString());
        absSender.execute(sendMessage);
    }
}
