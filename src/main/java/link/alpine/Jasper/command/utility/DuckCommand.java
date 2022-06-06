package link.alpine.Jasper.command.utility;

import link.alpine.Jasper.command.CommandClass;
import link.alpine.Jasper.util.CommandInfo;
import link.alpine.Jasper.util.CommandType;
import link.alpine.Jasper.util.EmbedUI;
import link.alpine.Jasper.util.LoggingManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.bitnick.net.duckduckgo.WebSearch;
import org.bitnick.net.duckduckgo.entity.SearchResult;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static link.alpine.Jasper.util.LoggingManager.slashLog;

/**
 * Helpful Search Command (Uses DDG API)
 * @author Laika
 */
public class DuckCommand extends CommandClass {
    private boolean enabled = true;

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String getName() {
        return "Duck";
    }

    @Override
    public void newCommand(String name, SlashCommandInteractionEvent e) {
        if ("search".equals(name)) {
            e.deferReply().queue();
            String option = e.getOption("query").getAsString();
            LoggingManager.slashLog(e, "with search \"" + option + "\".");
            WebSearch ws = WebSearch.instanceOf();
            SearchResult sr;
            try {
                sr = ws.instantAnswerSearch(option);
            } catch (Exception ex) {
                EmbedBuilder eb = new EmbedBuilder()
                        .setColor(EmbedUI.FAILURE)
                        .setTitle("Query Failed")
                        .setDescription("Couldn't find any results.")
                        .setFooter("Query: " + option)
                        .setTimestamp(ZonedDateTime.now());
                e.getHook().sendMessageEmbeds(eb.build()).queue();
                return;
            }
            EmbedBuilder eb = new EmbedBuilder()
                    .setColor(EmbedUI.INFO)
                    .setTitle(sr.getTitle(), sr.getUrl().toString())
                    .setDescription(sr.getDescription())
                    .setFooter("Query: " + option)
                    .setTimestamp(ZonedDateTime.now());
            e.getHook().sendMessageEmbeds(eb.build()).queue();
        }
    }

    @Override
    public List<CommandInfo> getSlashCommandInfo() {
        List<CommandInfo> cil = new ArrayList<>();
        CommandInfo ci = new CommandInfo("search", "Looks up with DuckDuckGo your query!", CommandType.COMMAND);
        ci.addOption("query", "The query to be searched", OptionType.STRING, true);
        cil.add(ci);
        return cil;
    }
}
