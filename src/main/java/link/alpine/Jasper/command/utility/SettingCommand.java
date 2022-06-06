package link.alpine.Jasper.command.utility;

import link.alpine.Jasper.Jasper;
import link.alpine.Jasper.command.CommandClass;
import link.alpine.Jasper.model.Server;
import link.alpine.Jasper.util.CommandInfo;
import link.alpine.Jasper.util.CommandType;
import link.alpine.Jasper.util.EmbedUI;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static link.alpine.Jasper.util.LoggingManager.slashLog;
import static link.alpine.Jasper.util.LoggingManager.slashResponse;

/**
 * Permits modification of server settings, critical class to functionality.
 * @author Laika
 */
public class SettingCommand extends CommandClass {
    @Override
    public boolean isEnabled() {
        return true; // Another non-disable command
    }

    @Override
    public String getName() {
        return "Settings";
    }

    @Override
    public void newCommand(String name, SlashCommandInteractionEvent e) {
        if(e.getGuild() != null) {
            Server server = Jasper.instance.getServerManager().getOrCreateServer(e.getGuild());
            switch (name) {
                case "settings" -> {
                    slashLog(e);
                    e.deferReply().queue();
                    // No options, just fire an embed off...
                    EmbedBuilder eb = new EmbedBuilder()
                            .setColor(EmbedUI.INFO)
                            .setTitle(Jasper.instance.name + " Settings")
                            .setDescription(server.getOpts())
                            .setFooter(EmbedUI.BRAND)
                            .setTimestamp(ZonedDateTime.now());
                    e.getHook().sendMessageEmbeds(eb.build()).queue();
                }
                case "setting" -> {
                    // User is attempting a settings modification. Check if admin.
                    if(!e.getMember().hasPermission(Permission.ADMINISTRATOR) && !e.getMember().isOwner()) {
                        slashLog(e, EmbedUI.RESPONSE_PRIVILEGES);
                        e.deferReply(true).queue();
                        // Private reply, other people can't see this if ephemeral.
                        e.getHook().sendMessage("**You cannot run this command.**").queue();
                        return;
                    }
                    switch (e.getSubcommandName().toLowerCase()) {
                        case "view" -> {
                            e.deferReply().queue();
                            String opt = e.getOption("name").getAsString();
                            slashLog(e, "with subcommand \"" + e.getSubcommandName().toLowerCase() + "\" for setting \"" + opt + "\".");
                            EmbedBuilder eb1 = new EmbedBuilder()
                                    .setColor(EmbedUI.INFO)
                                    .setTitle(opt)
                                    .setDescription("Value: `" + server.getOptionByString(opt) + '`')
                                    .setFooter(EmbedUI.BRAND)
                                    .setTimestamp(ZonedDateTime.now());
                            e.getHook().sendMessageEmbeds(eb1.build()).queue();
                        }
                        case "set" -> {
                            e.deferReply().queue();
                            String opt1 = e.getOption("name").getAsString();
                            String opt2 = e.getOption("value").getAsString();
                            slashLog(e, "with subcommand \"" + e.getSubcommandName().toLowerCase() + "\" for setting \"" + opt1 + "\" to \"" + opt2 + "\".");
                            String response = server.setOptionByString(opt1, opt2);
                            slashResponse(e, response);
                            EmbedBuilder eb2 = new EmbedBuilder()
                                    .setColor(EmbedUI.SUCCESS)
                                    .setTitle(opt1)
                                    .setDescription(response)
                                    .setFooter(EmbedUI.BRAND)
                                    .setTimestamp(ZonedDateTime.now());
                            e.getHook().sendMessageEmbeds(eb2.build()).queue();
                        }
                        case "clear" -> {
                            e.deferReply().queue();
                            String opt3 = e.getOption("name").getAsString();
                            slashLog(e, "with subcommand \"" + e.getSubcommandName().toLowerCase() + "\" for setting \"" + opt3 + "\".");
                            String response1 = server.resetOptionByString(opt3);
                            slashResponse(e, response1);
                            EmbedBuilder eb3 = new EmbedBuilder()
                                    .setColor(EmbedUI.SUCCESS)
                                    .setTitle(opt3)
                                    .setDescription(response1)
                                    .setFooter(EmbedUI.BRAND)
                                    .setTimestamp(ZonedDateTime.now());
                            e.getHook().sendMessageEmbeds(eb3.build()).queue();
                        }
                    }
                }
            }
        }
    }

    @Override
    public List<CommandInfo> getSlashCommandInfo() {
        List<CommandInfo> si = new ArrayList<>();

        CommandInfo ci2 = new CommandInfo("setting", "Permits modification, viewing, and clearing of settings.", CommandType.COMMAND);
        // For those looking here for inspiration, you CANNOT mix options and subcommands. You can only have one or the other.

        CommandInfo ci = new CommandInfo("view", "Shows the current value for the setting provided.", CommandType.SUBCOMMAND);
        ci.addOption("name", "The name of the setting to display", OptionType.STRING, true);
        ci2.addSubcommand(ci);

        CommandInfo ci3 = new CommandInfo("set", "sets a setting for the guild you are in", CommandType.SUBCOMMAND);
        ci3.addOption("name", "The name of the setting to modify", OptionType.STRING, true);
        ci3.addOption("value", "The value to set the setting to", OptionType.STRING, true);
        ci2.addSubcommand(ci3);

        CommandInfo ci4 = new CommandInfo("clear", "reverts a setting back to its default value", CommandType.SUBCOMMAND);
        ci4.addOption("name", "Name of the setting to clear", OptionType.STRING, true);
        ci2.addSubcommand(ci4);

        CommandInfo ci5 = new CommandInfo("settings", "displays all settings available for the guild.", CommandType.COMMAND);
        si.add(ci5);

        si.add(ci2);
        return si;
    }
}
