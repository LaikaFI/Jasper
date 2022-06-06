package link.alpine.Jasper.command;

import link.alpine.Jasper.util.CommandInfo;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;

/**
 * CommandClass Abstract Class
 * Partial SHIRO import, rewritten in accordance for SlashCommands.
 * Use this for any commands you make, and ensure they go under the .command class.
 */
public abstract class CommandClass extends ListenerAdapter {

    //Is the command enabled?
    public abstract boolean isEnabled();

    //Whats the name of the Command Package (Specify if multiple, like "Utility" or "Moderation")
    public abstract String getName();

    /**
     * For new slash commands
     * @param name - name of the command executed
     * @param e - SlashCommandInteractionEvent, used for all references.
     */
    public abstract void newCommand(String name, SlashCommandInteractionEvent e);

    /**
     * Overriding our SlashCommandInteractionEvent that way it can execute a cmd.
     * @param event
     */
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        newCommand(event.getName(), event);
    }

    /**
     * Also for the sake of organization. To upsert slash commands.
     * Follow as name, description. (for upsertCommand(name, description);
     * @return - The name and description for the commands contained within the class.
     */
    public abstract List<CommandInfo> getSlashCommandInfo();
}
