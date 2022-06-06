package link.alpine.Jasper.command.fun;

import link.alpine.Jasper.command.CommandClass;
import link.alpine.Jasper.util.CommandInfo;
import link.alpine.Jasper.util.CommandType;
import link.alpine.Jasper.util.EmbedUI;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static link.alpine.Jasper.util.LoggingManager.slashLog;

/**
 * Random User Choice Command
 * @author Laika
 */
public class FightCommand extends CommandClass {
    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return "Fight";
    }

    @Override
    public void newCommand(String name, SlashCommandInteractionEvent e) {
        if ("fight".equals(name)) {
            slashLog(e);
            e.deferReply().queue();
            List<User> usersForRng = new ArrayList<>();
            List<String> userNames = new ArrayList<>();
            for (OptionMapping option : e.getOptions()) {
                usersForRng.add(option.getAsUser());
                userNames.add(option.getAsUser().getName());
            }
            // Done, now roll
            var eb = new EmbedBuilder()
                    .setColor(EmbedUI.INFO)
                    .setTitle("Match in progress...")
                    .setDescription("*POW! KABLAM! SCHNARF!*")
                    .setFooter(EmbedUI.BRAND)
                    .setTimestamp(ZonedDateTime.now());
            e.getHook().sendMessageEmbeds(eb.build()).queue();
            var rng = new Random();
            var pickedUser = usersForRng.get(rng.nextInt(usersForRng.size()));
            var eb1 = new EmbedBuilder()
                    .setColor(EmbedUI.INFO)
                    .setTitle("FATALITY!")
                    .setDescription(pickedUser.getName() + " wins!")
                    .setThumbnail(pickedUser.getAvatarUrl())
                    .setFooter(EmbedUI.BRAND)
                    .setTimestamp(ZonedDateTime.now());
            e.getHook().editOriginalEmbeds(eb1.build()).completeAfter(3, TimeUnit.SECONDS);
        }
    }

    @Override
    public List<CommandInfo> getSlashCommandInfo() {
        List<CommandInfo> cil = new ArrayList<>();
        var ci = new CommandInfo("fight", "MORTALLL KOMBATTTT", CommandType.COMMAND);
        ci.addOption("value1", "first fighter", OptionType.USER, true);
        ci.addOption("value2", "second fighter", OptionType.USER, true);
        ci.addOption("value3", "third fighter", OptionType.USER, false);
        ci.addOption("value4", "fourth fighter", OptionType.USER, false);
        cil.add(ci);
        return cil;
    }
}
