package link.alpine.Jasper.listener;

import link.alpine.Jasper.Jasper;
import link.alpine.Jasper.JasperConfig;
import link.alpine.Jasper.util.EmbedUI;
import link.alpine.Jasper.util.LoggingManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Skynet Listener
 * Handles all event logging (Join/Leaves, Username Change, etc).
 * @author Created by a dumbass, who wanted to basically spy on discords using an event shitshow, i instead now use their shit code for debug.
 */
public class SkynetListener extends ListenerAdapter {

    JasperConfig config;
    DateTimeFormatter dTF = DateTimeFormatter.ofPattern("MM-dd-yyyy");

    /**
     * Join/Leave logging
     * Requires the server to configure welcomeChannel (optionally joinRole).
     */
    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        var guild = event.getGuild();
        var server = Jasper.instance.getServerManager().getOrCreateServer(guild);
        var member = event.getMember();
        LoggingManager.info("User [" + event.getUser().getName() + ":" + event.getUser().getId() + "] joined guild [" + guild.getName() + ':' + guild.getId() + "].");
        if(server.getJoinRole() != null) {
            try {
                guild.addRoleToMember(member, guild.getRoleById(server.getJoinRole())).queue();
            } catch(Exception ex) {
                LoggingManager.error("Failed to apply welcome role to " + member.getEffectiveName() + ", as the role does not exist.");
            }
        }
        if(server.getWelcomeChannel() != null) {
            // Fetch the welcome channel from settings.
            TextChannel textChannel;
            try {
                textChannel = guild.getTextChannelById(server.getWelcomeChannel());
            } catch (Exception ex) {
                LoggingManager.error("Failed to send join message to guild " + guild.getId() + " as the welcome channel was not found.");
                return;
            }
            // Prepare embed.
            var eb = new EmbedBuilder()
                    .setColor(EmbedUI.SUCCESS)
                    .setAuthor(member.getEffectiveName() + "#" + member.getUser().getDiscriminator() + " ("
                            + member.getId() + ")", null, event.getUser().getAvatarUrl()) // Url cannot be member.
                    .setDescription(member.getAsMention() + " | **Joined Discord**: " + member.getTimeCreated().format(dTF))
                    .setFooter("User Joined")
                    .setTimestamp(OffsetDateTime.now());
            textChannel.sendMessageEmbeds(eb.build()).queue();
            LoggingManager.debug("Guild join message successfully sent.");
        }
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        var guild = event.getGuild();
        LoggingManager.info("User [" + event.getUser().getName() + ":" + event.getUser().getId() + "] left guild [" + guild.getName() + ':' + guild.getId() + "].");
        var server = Jasper.instance.getServerManager().getOrCreateServer(guild);
        if(server.getWelcomeChannel() != null) {
            // Fetch the welcome channel from settings.
            TextChannel textChannel;
            var member = event.getMember();
            try {
                textChannel = guild.getTextChannelById(server.getWelcomeChannel());
            } catch (Exception ex) {
                LoggingManager.error("Failed to send leave message to guild " + guild.getId() + " as the welcome channel was not found.");
                return;
            }
            // Prepare embed.
            var eb = new EmbedBuilder()
                    .setColor(EmbedUI.FAILURE)
                    .setAuthor(member.getEffectiveName() + "#" + member.getUser().getDiscriminator() + " ("
                            + member.getId() + ")", null, event.getUser().getAvatarUrl()) // Url cannot be member.
                    .setDescription(member.getAsMention() + " | **Joined Server**: " + member.getTimeJoined().format(dTF))
                    .setFooter("User Left")
                    .setTimestamp(OffsetDateTime.now());
            textChannel.sendMessageEmbeds(eb.build()).queue();
            LoggingManager.debug("Guild leave message successfully sent.");
        }
    }

    /**
     * Name change logging
     * Sent to the central logChannel.
     */
    @Override
    public void onUserUpdateName(@NotNull UserUpdateNameEvent event) {
        var user = event.getUser();
        String name = event.getNewName(), oldName = event.getOldName();
        LoggingManager.info("User [" + name + ':' + user.getId() + "] changed name from \"" + oldName + "\".");
        var logChannel = Jasper.JDA.getTextChannelById(config.getLogChannel());

        var eb = new EmbedBuilder()
                .setColor(EmbedUI.INFO)
                .setAuthor(user.getName() + '#' + user.getDiscriminator() + " (" + user.getId() + ')', null, user.getAvatarUrl())
                .setDescription('`' + oldName + "` → `" + name + '`')
                .setFooter("Username Edited")
                .setTimestamp(OffsetDateTime.now());
        logChannel.sendMessageEmbeds(eb.build()).queue();
        LoggingManager.debug("Username edit message successfully sent.");
    }
}
