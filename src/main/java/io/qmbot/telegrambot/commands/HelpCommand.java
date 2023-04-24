package io.qmbot.telegrambot.commands;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
@Component
public class HelpCommand extends BotCommand {
    public HelpCommand() {
        super("help", "Test");
    }

    @Override
    public void execute(AbsSender absSender, Message message, String[] arguments) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();

        StartCommand startCommand = new StartCommand();
        FeedbackCommand feedbackCommand = new FeedbackCommand();
        ShowReactionsCommand showReactionsCommand = new ShowReactionsCommand();
        AboutCommand aboutCommand = new AboutCommand();
        AddReactionCommand addReactionCommand = new AddReactionCommand();

        sendMessage.setChatId(message.getChat().getId());
        sendMessage.setText("Available commands:\n"
                + "/" + getCommandIdentifier() + " - " + getDescription() + "\n"
                + "/" + startCommand.getCommandIdentifier() + " - " + startCommand.getDescription() + "\n"
                + "/" + feedbackCommand.getCommandIdentifier() + " - " + feedbackCommand.getDescription() + "\n"
                + "/" + showReactionsCommand.getCommandIdentifier() + " - " + showReactionsCommand.getDescription() + "\n"
                + "/" + addReactionCommand.getCommandIdentifier() + " - " + addReactionCommand.getDescription() + "\n"
                + "/" + aboutCommand.getCommandIdentifier() + " - " + aboutCommand.getDescription() + "\n");

        absSender.execute(sendMessage);

    }
}
