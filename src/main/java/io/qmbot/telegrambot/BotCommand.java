package io.qmbot.telegrambot;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Representation of a command, which can be executed.
 *
 * @author Timo Schulz (Mit0x2)
 */
public abstract class BotCommand implements IBotCommand {
    private static final Logger logger = LoggerFactory.getLogger(BotCommand.class);
    public static final String COMMAND_INIT_CHARACTER = "/";
    public static final String COMMAND_PARAMETER_SEPARATOR_REGEXP = "\\s+";
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
    public BotCommand(String commandIdentifier, String description) {

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

        this.commandIdentifier = commandIdentifier.toLowerCase();
        this.description = description;
    }

    /**
     * Get the identifier of this command.
     *
     * @return the identifier
     */
    public final String getCommandIdentifier() {
        return commandIdentifier;
    }

    /**
     * Get the description of this command.
     *
     * @return the description as String
     */
    public final String getDescription() {
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
            logger.error("Failed to execute", e);
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
