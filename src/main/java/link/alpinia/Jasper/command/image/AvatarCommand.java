package link.alpinia.Jasper.command.image;

import link.alpinia.Jasper.util.EmbedUI;
import link.alpinia.SlashComLib.CommandClass;
import link.alpinia.SlashComLib.CommandInfo;
import link.alpinia.SlashComLib.CommandType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static link.alpinia.Jasper.util.LoggingManager.slashLog;

/**
 * Helpful Avatar grabber command
 * @author dumbass
 */
public class AvatarCommand extends CommandClass {
    @Override
    public boolean isEnabled() { return true; }

    @Override
    public String getName() { return "Avatar"; }

    @Override
    public void newCommand(String name, SlashCommandInteractionEvent e) {
        if ("avatar".equals(name)) {
            e.deferReply().queue();

            final var user = e.getOptions().size() == 0
                    ? e.getUser()
                    : e.getOption("user").getAsUser();
            slashLog(e, "for user [" + user.getName() + ":" + user.getId() + "]." );

            var eb = new EmbedBuilder()
                    .setColor(EmbedUI.INFO)
                    .setAuthor(user.getName() + "#" + user.getDiscriminator())
                    .setImage(user.getEffectiveAvatarUrl() + "?size=2048")
                    .setFooter(EmbedUI.BRAND)
                    .setTimestamp(ZonedDateTime.now());
            e.getHook().sendMessageEmbeds(eb.build()).queue();
        }
    }

    @Override
    public List<CommandInfo> getSlashCommandInfo() {
        List<CommandInfo> cil = new ArrayList<>();
        var ci = new CommandInfo("avatar", "Returns the avatar of the specified user.", CommandType.COMMAND);
        ci.addOption("user", "User to fetch.", OptionType.USER, false);
        cil.add(ci);
        return cil;
    }
}
