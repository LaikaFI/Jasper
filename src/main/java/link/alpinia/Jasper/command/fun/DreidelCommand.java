package link.alpinia.Jasper.command.fun;

import link.alpinia.Jasper.util.EmbedUI;
import link.alpinia.SlashComLib.CommandClass;
import link.alpinia.SlashComLib.CommandInfo;
import link.alpinia.SlashComLib.CommandType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static link.alpinia.Jasper.util.LoggingManager.slashLog;

/**
 * Dreidel Dreidel...
 * @author dumbass^2
 */
public class DreidelCommand extends CommandClass {
    private final List<String> sides;

    public DreidelCommand() {
        List<String> sides = new ArrayList<>();
        sides.add("Nun");
        sides.add("Gimmel");
        sides.add("Hay");
        sides.add("Shin");
        this.sides = sides;
    }

    @Override
    public boolean isEnabled() { return true; }

    @Override
    public String getName() { return "Dreidel"; }

    @Override
    public void newCommand(String name, SlashCommandInteractionEvent e) {
        if ("dreidel".equals(name)) {
            slashLog(e);
            e.deferReply().queue();
            var eb = new EmbedBuilder()
                    .setColor(EmbedUI.INFO)
                    .setTitle("Spinning...")
                    .setDescription("*brrrrrrrrrrrrrr*")
                    .setFooter(EmbedUI.BRAND)
                    .setTimestamp(ZonedDateTime.now());
            e.getHook().sendMessageEmbeds(eb.build()).queue();
            var rand = new Random();
            var result = sides.get(rand.nextInt(sides.size()));
            var eb1 = new EmbedBuilder()
                    .setColor(EmbedUI.INFO)
                    .setTitle("You rolled...")
                    .setDescription(result + "!")
                    .setFooter(EmbedUI.BRAND)
                    .setTimestamp(ZonedDateTime.now());
            e.getHook().editOriginalEmbeds(eb1.build()).completeAfter(3, TimeUnit.SECONDS);
        }
    }

    @Override
    public List<CommandInfo> getSlashCommandInfo() {
        List<CommandInfo> si = new ArrayList<>();
        si.add(new CommandInfo("dreidel", "Spins a dreidel!", CommandType.COMMAND));
        return si;
    }
}
