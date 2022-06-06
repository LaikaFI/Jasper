package link.alpine.Jasper.util;

import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.HashMap;

/**
 * Helpful CommandInfo class to easily make slash commands.
 * @author Kay
 */
public class CommandInfo {
    private String name;
    private String description;
    private CommandType type;
    private HashMap<String, OptionType> options;
    private HashMap<String, String> optionDescriptions;
    private HashMap<String, Boolean> optionRequirements;
    private HashMap<String, CommandInfo> subCommands;

    /**
     * Constructor to build CommandInfo with.
     * @param name - Name of the slash command (MUST BE ALL LOWERCASE)
     * @param description - Description of the slash command
     */
    public CommandInfo(String name, String description, CommandType type) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.options = new HashMap<>();
        this.optionDescriptions = new HashMap<>();
        this.optionRequirements = new HashMap<>();
        this.subCommands = new HashMap<>();
    }

    /**
     * Returns whether the command has options/input.
     * @return - boolean
     */
    public boolean hasOptions() {
        return options.size() != 0;
    }

    public boolean hasSubCommands() { return subCommands.size() != 0; }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public HashMap<String, OptionType> getOptions() {
        return options;
    }

    public HashMap<String, Boolean> getOptionRequirements() {
        return optionRequirements;
    }

    public HashMap<String, String> getOptionDescriptions() {
        return optionDescriptions;
    }

    public HashMap<String, CommandInfo> getSubCommands() { return subCommands; }

    /**
     * The way you add options to a command. Use this function for EACH argument.
     * @param name - name of the field
     * @param description - description for the field
     * @param type - the OptionType of the field (e.x. OptionType.STRING, OptionType.CHANNEL, etc.)
     * @param required - whether the command can be run without the field or not.
     */
    public void addOption(String name, String description, OptionType type, boolean required) {
        options.put(name, type);
        optionDescriptions.put(name, description);
        optionRequirements.put(name, required);
    }

    /**
     * The way you add subcommands to a command. NOTE you cannot have options for the command if you use this.
     * @param cmdInfo - The CommandInfo for the subcommand (including options).
     */
    public void addSubcommand(CommandInfo cmdInfo) {
        subCommands.put(cmdInfo.name, cmdInfo);
    }
}
