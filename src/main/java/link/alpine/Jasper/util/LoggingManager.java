package link.alpine.Jasper.util;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

/**
 * Logging Class
 * Provides static logging & extensible template messages.
 * @author dumbass
 * @apiNote the only SEMI decent thing dumbass made, and it's literally just a logger helper. :thinking:
 */
public class LoggingManager {

    private static final Logger logger = LoggerFactory.getLogger("Jasper");

    // Static logging reference
    public static void debug(String str) { logger.debug(str); }
    public static void info(String str) { logger.info(str); }
    public static void warn(String str) { logger.warn(str); }
    public static void error(String str) { logger.error(str); }

    /**
     * Used for logging commands with ease to console via a static method.
     * @param event - the event ran
     * @param msg - Any message to append with this.
     */
    public static void slashLog(SlashCommandInteractionEvent event, @Nullable String msg) {
        var user = event.getUser();
        info("""
                User [%s:%s] ran command: "%s" %s"""
                .formatted(user.getName(), user.getId(), event.getName(), msg == null ? "" : msg));
    }

    public static void slashLog(SlashCommandInteractionEvent event) {
        var user = event.getUser();
        info("""
                User [%s:%s] ran command: "%s"."""
                .formatted(user.getName(), user.getId(), event.getName()));
    }

    public static void slashResponse(SlashCommandInteractionEvent event, String msg) {
        var user = event.getUser();
        info("""
                User [%s:%s] was provided response: "%s\"""".formatted(user.getName(), user.getId(), msg));
    }
}