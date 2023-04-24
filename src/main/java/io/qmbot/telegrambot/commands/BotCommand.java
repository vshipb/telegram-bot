package io.qmbot.telegrambot.commands;

import java.io.IOException;
import java.util.Locale;

import io.qmbot.telegrambot.Bot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Representation of a command, which can be executed.
 */
public abstract class BotCommand implements IBotCommand {
    private static final Logger logger = LoggerFactory.getLogger(BotCommand.class);
    private static final String COMMAND_INIT_CHARACTER = "/";
    private static final int COMMAND_MAX_LENGTH = 32;

    private final String commandIdentifier;
    private final String description;

    /**
     * Construct a command.
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     */
    BotCommand(String commandIdentifier, String description) {

        if (commandIdentifier == null || commandIdentifier.isEmpty()) {
            throw new IllegalArgumentException("commandIdentifier for command cannot be null or empty");
        }

        if (commandIdentifier.startsWith(COMMAND_INIT_CHARACTER)) {
            commandIdentifier = commandIdentifier.substring(1);
        }

        if (commandIdentifier.length() + 1 > COMMAND_MAX_LENGTH) {
            throw new IllegalArgumentException("commandIdentifier cannot be longer than " + COMMAND_MAX_LENGTH
                    + " (including " + COMMAND_INIT_CHARACTER + ")");
        }

        this.commandIdentifier = commandIdentifier.toLowerCase(Locale.ROOT);
        this.description = description;
    }

    /**
     * Get the identifier of this command.
     *
     * @return the identifier
     */
    public String getCommandIdentifier() {
        return commandIdentifier;
    }

    /**
     * Get the description of this command.
     *
     * @return the description as String
     */
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "<b>" + COMMAND_INIT_CHARACTER + getCommandIdentifier()
                + "</b>\n" + getDescription();
    }

    /**
     * Process the message and execute the command.
     *
     * @param absSender absSender to send messages over
     * @param message   the message to process
     * @param arguments passed arguments
     */
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        try {
            execute(absSender, message, arguments);
        } catch (TelegramApiException | IOException e) {
            logger.error(Bot.failedToExecute, e);
        }
    }

    /**
     * Execute the command.
     *
     * @param absSender absSender to send messages over
     * @param message message
     * @param arguments passed arguments
     */
    public abstract void execute(AbsSender absSender, Message message, String[] arguments) throws TelegramApiException, IOException;
}
