package link.alpine.Jasper.command.utility;

import link.alpine.Jasper.Jasper;
import link.alpine.Jasper.util.EmbedUI;
import link.alpinia.SlashComLib.CommandClass;
import link.alpinia.SlashComLib.CommandInfo;
import link.alpinia.SlashComLib.CommandType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static link.alpine.Jasper.util.LoggingManager.slashLog;

public class HelpCommand extends CommandClass {
    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return "Help";
    }

    @Override
    public void newCommand(String name, SlashCommandInteractionEvent e) {
        if ("help".equalsIgnoreCase(name)) {
            slashLog(e);
            e.deferReply().queue();
            // We assemble a help string, this is our description for our embed.
            var sb = new StringBuilder();
            for(CommandClass cc : Jasper.instance.activeCommands) {
                for(CommandInfo ci : cc.getSlashCommandInfo()) {
                    var cname = ci.getName();
                    var cdesc=  ci.getDescription();
                    sb.append("**").append(cname).append("** - __").append(cdesc).append("__\n");
                    if(ci.hasOptions()) {
                        for (String opt : ci.getOptions().keySet()) {
                            sb.append("-> **").append(opt).append("** `").append(ci.getOptions().get(opt).getName())
                                    .append("` *").append(ci.getOptions().get(opt).getDescription()).append("*");
                            if (ci.getOptions().get(opt).isRequired()) {
                                sb.append(" __***[REQUIRED]***__");
                            }
                            sb.append("\n");
                        }
                    }
                    if(ci.hasSubCommands()) {
                        for (String subc : ci.getSubCommands().keySet()) {
                            var subCommand = ci.getSubCommands().get(subc);
                            sb.append("-> **").append(subc).append("** *").append(subCommand.getDescription()).append("*\n");
                            if (subCommand.hasOptions()) {
                                for (String opt : subCommand.getOptions().keySet()) {
                                    sb.append("--> **").append(opt).append("** `").append(subCommand.getOptions().get(opt).getName())
                                            .append("` *").append(subCommand.getOptions().get(opt).getDescription()).append("*");
                                    if (subCommand.getOptions().get(opt).isRequired()) {
                                        sb.append(" __***[REQUIRED]***__");
                                    }
                                    sb.append("\n");
                                }
                            }
                            sb.append("\n");
                        }
                    }
                    sb.append("\n");
                }
            }
            EmbedBuilder eb = new EmbedBuilder()
                    .setColor(EmbedUI.INFO)
                    .setTimestamp(ZonedDateTime.now())
                    .setFooter(EmbedUI.BRAND)
                    .setTitle("Help Command")
                    .setDescription(sb.toString());
            e.getHook().sendMessageEmbeds(eb.build()).queue();
        }
    }

    @Override
    public List<CommandInfo> getSlashCommandInfo() {
        List<CommandInfo> cil = new ArrayList<>();
        var ci = new CommandInfo("help", "Displays all enabled commands this bot has.", CommandType.COMMAND);
        cil.add(ci);
        return cil;
    }
}
