package link.alpinia.Jasper.listener;

import link.alpinia.Jasper.Jasper;
import link.alpinia.Jasper.util.EmbedUI;
import link.alpinia.Jasper.util.LoggingManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Main Listener
 * Used for all essential utility listeners, such as guild handling and persistence.
 * @author Laika, also fucked up by TWO dumbasses.
 */
public class MainListener extends ListenerAdapter {

    private static final String dateFormat = "MM.dd.yyyy";

    /**
     * GuildJoin event listener, that ensures that a discord has a profile created for it.
     * @param event - event to be handled...
     */
    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        // Automatically create our default information for the server if we don't have it already.
        LoggingManager.info("Joined a new guild, NAME: " + event.getGuild().getName() + " ID: " + event.getGuild().getId());
        Jasper.instance.getServerManager().createNewDefaultServer(event.getGuild());
        Jasper.instance.registerForGuild(event.getGuild());
    }

    /**
     * Shoots a message into console when the bot is defined as "Ready" by Discord.
     */
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        LoggingManager.info("""
                Received READY signal from Discord, bot is now logged in.
                --------------------------------
                Active Guilds: [%s]
                Guilds Unavailable: [%s]
                --------------------------------""".formatted(event.getGuildAvailableCount(), event.getGuildUnavailableCount()));
    }

    /**
     * Quick Response for if someone pings me.
     */
    public void onMessageReceived(MessageReceivedEvent event) {
        if(event.getMessage().getMentionedUsers().contains(Jasper.JDA.getSelfUser())) {
            LoggingManager.info("Sent about message in " + event.getGuild().getId());
            var eb = new EmbedBuilder()
                    .setColor(EmbedUI.INFO)
                    .setTitle("Hi, i'm " + Jasper.instance.name + "!")
                    .setDescription("I was summoned on " + Jasper.JDA.getSelfUser().getTimeCreated().format(DateTimeFormatter.ofPattern(dateFormat))
                            + "! I use slash commands, so feel free to use those!")
                    .setThumbnail(Jasper.JDA.getSelfUser().getAvatarUrl())
                    .setTimestamp(ZonedDateTime.now())
                    .setFooter(EmbedUI.BRAND);
            event.getChannel().sendMessageEmbeds(eb.build()).queue();
        }
    }
}
