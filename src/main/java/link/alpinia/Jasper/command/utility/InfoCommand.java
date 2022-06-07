package link.alpinia.Jasper.command.utility;

import link.alpinia.Jasper.util.EmbedUI;
import link.alpinia.Jasper.util.LoggingManager;
import link.alpinia.SlashComLib.CommandClass;
import link.alpinia.SlashComLib.CommandInfo;
import link.alpinia.SlashComLib.CommandType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static link.alpinia.Jasper.util.LoggingManager.slashLog;

/**
 * Helpful User Information Command
 * @author dumbass
 */
public class InfoCommand extends CommandClass {
    @Override
    public boolean isEnabled() { return true; }

    @Override
    public String getName() { return "Info"; }

    @Override
    public void newCommand(String name, SlashCommandInteractionEvent e) {
        switch (name){
            case "userinfo":
                e.deferReply().queue();

                // Setup variables
                final var member = e.getOptions().size() == 0
                        ? e.getMember()
                        : e.getOption("user").getAsMember();
                final var dTF = DateTimeFormatter.ofPattern("MM-dd-yyyy");
                LoggingManager.slashLog(e, "for user [" + member.getUser().getName() + ":" + member.getId() + "].");
                // Build embed
                EmbedBuilder eb1 = new EmbedBuilder()
                        .setColor(EmbedUI.INFO)
                        .setAuthor(member.getEffectiveName() + "#" + member.getUser().getDiscriminator())
                        .addField("Joined Server", member.getTimeJoined().format(dTF), true)
                        .addField("Joined Discord", member.getTimeCreated().format(dTF), true)
                        .addField("Roles", member.getRoles().stream().map(Role::getName).reduce((a, b) -> a + ", " + b).orElse("None"), false)
                        .setThumbnail(member.getEffectiveAvatarUrl())
                        .setFooter("ID: " + member.getId())
                        .setTimestamp(ZonedDateTime.now());
                e.getHook().sendMessageEmbeds(eb1.build()).queue();
                return;
            case "serverinfo":
                e.deferReply().queue();
                // Setup variables
                final var guild = e.getGuild();
                final var dTF2 = DateTimeFormatter.ofPattern("MM-dd-yyyy");
                LoggingManager.slashLog(e, "for guild [" + guild.getId() + ":" + guild.getName() + "].");
                // Build Embed
                EmbedBuilder eb2 = new EmbedBuilder()
                        .setColor(EmbedUI.INFO)
                        .setAuthor(guild.getName())
                        .addField("Members", "**" + guild.getMemberCount() + "** member(s) \n Owner: "
                                + guild.getOwner().getEffectiveName() + "#" + guild.getOwner().getUser().getDiscriminator(), true)
                        .addField("Channels", "**" + guild.getTextChannels().size() + "** Text \n **"
                                + guild.getVoiceChannels().size() + "** Voice", true)
                        .addField("Other", "Roles: **" + guild.getRoles().size() + "** \n"
                                + "Created: " + guild.getTimeCreated().format(dTF2), true)
                        .setThumbnail(guild.getIconUrl())
                        .setFooter("ID: " + guild.getId());
                e.getHook().sendMessageEmbeds(eb2.build()).queue();

        }
    }

    @Override
    public List<CommandInfo> getSlashCommandInfo() {
        List<CommandInfo> cil = new ArrayList<>();
        CommandInfo ci = new CommandInfo("userinfo", "Returns information about a user.", CommandType.COMMAND);
        ci.addOption("user", "The user to get information about.", OptionType.USER, false);

        CommandInfo ci2 = new CommandInfo("serverinfo", "Returns information about the server.", CommandType.COMMAND);

        cil.add(ci);
        cil.add(ci2);

        return cil;
    }
}
