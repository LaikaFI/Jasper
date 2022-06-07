package link.alpine.Jasper.command.fun;

import link.alpinia.SlashComLib.CommandClass;
import link.alpinia.SlashComLib.CommandInfo;
import link.alpinia.SlashComLib.CommandType;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.apache.logging.log4j.core.config.plugins.convert.HexConverter;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ColourCommand extends CommandClass {
    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return "Color";
    }

    @Override
    public void newCommand(String s, SlashCommandInteractionEvent e) {
        if(s.equals("color")) {
            e.deferReply(true).queue(); //It's a color, we don't need it to be "AAAAAAAAA PUBLIC"
            var starter = e.getOption("color").getAsString();
            var hex = starter.startsWith("#") ? starter : "#" + starter;
            Color toColour;
            try {
                toColour = Color.decode(hex);
            } catch (NumberFormatException ex) {
                e.getHook().sendMessage("**You need to provide a valid HEX color code!**").queue();
                return;
            }
            for(Role role : e.getMember().getRoles()) {
                try {
                    Color test = Color.decode(role.getName());
                    e.getGuild().removeRoleFromMember(e.getMember(), role).queue();
                } catch (NumberFormatException ex) {
                    //it's not a colour.
                }
            }
            trySetRole(toColour, hex, e);
            e.getHook().sendMessage("**Set your color to `" + hex + "` successfully.**").queue();
        }
    }

    private void trySetRole(Color color, String hex, SlashCommandInteractionEvent e) {
        if(e.getGuild().getRolesByName(hex, true).size() != 0) {
            //role already exists.
            e.getGuild().addRoleToMember(e.getMember(), e.getGuild().getRolesByName(hex, true).get(0)).queue();
            return;
        } else {
            //Create new role.
            Role role = e.getGuild().createRole().setColor(color).setName(hex).complete();
            e.getGuild().addRoleToMember(e.getMember(), role).queue();
        }
    }

    @Override
    public List<CommandInfo> getSlashCommandInfo() {
        List<CommandInfo> cis = new ArrayList<>();
        var ci = new CommandInfo("color", "Lets you create a custom role that sets your color.", CommandType.COMMAND);
        ci.addOption("color", "the color you would like, in HEX format.", OptionType.STRING, true);
        cis.add(ci);
        return cis;
    }
}
