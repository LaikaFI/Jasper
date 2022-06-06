package link.alpine.Jasper.util;

/**
 * Used to identify what type of Command is being used.
 * This is intended to prevent JDA errors.
 */
public enum CommandType {

    /**
     * Commands that are registered using the upsertCommand method.
     */
    COMMAND,

    /**
     * Commands that fall under above commands using the addSubcommand function.
     */
    SUBCOMMAND
}
