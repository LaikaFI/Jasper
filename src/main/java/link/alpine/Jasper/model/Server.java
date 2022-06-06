package link.alpine.Jasper.model;

import link.alpine.Jasper.Jasper;

import javax.annotation.Nullable;

/**
 * Server Class
 * Used for in-memory data storage. Loaded from Database later.
 * @author Laika
 * @implNote This class is where all server info is stored per-server, so be liberal with additions.
 */
public class Server {
    // Guild ID
    private String id;
    // The channel for welcome logs to be posted to.
    private String welcomeChannel;
    // The role to be assigned on join, if null ignored.
    private String joinRole;
    //Moderation role, used for /mod
    private String modChannel;
    //If the server has been modified in memory, for saving persistently.
    private boolean modified;

    /**
     * Default constructor, used for new servers
     * @param id - the guild id to have server constructed for.
     */
    public Server(String id) {
        this.id = id;
        this.welcomeChannel = null;
        this.modChannel = null;
        this.joinRole = null;
        this.modified = false;
    }

    /**
     * Database Constructor
     * @param id - id of the server
     * @param welcomeChannel - channel for welcome messages, if enabled
     */
    public Server(String id, @Nullable String welcomeChannel, @Nullable String joinRole, String modChannel) {
        this.id = id;
        this.welcomeChannel = welcomeChannel;
        this.modChannel = modChannel;
        this.joinRole = joinRole;
        this.modified = false;
    }

    public String getId() { return id; }

    public String getJoinRole() { return joinRole; }

    public String getModChannel() { return modChannel; }

    public String getWelcomeChannel() {
        return welcomeChannel;
    }

    public void setJoinRole(String joinRole) {
        this.modified = true;
        this.joinRole = joinRole;
    }

    public void setWelcomeChannel(String welcomeChannel) {
        this.modified = true;
        this.welcomeChannel = welcomeChannel;
    }

    /**
     * Checks the modification of the server file in memory
     * @return - whether the server settings have been modified
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * Options in the server class that can be modified.
     * @return - Options in a string that can be printed to discord.
     */
    public String getOpts() {
        return """
                welcomeChannel - the channel to send welcome messages to
                modChannel - the channel to send moderation logs to
                joinRole - the role to apply to new members who join this guild""";
    }

    /**
     * Fetches the option value by string for discord command.
     * @param string - the string to assess
     * @return - the value (if applicable) to return
     */
    public String getOptionByString(String string) {
        return switch (string.toLowerCase()) {
            case "joinrole" -> joinRole;
            case "welcomechannel" -> welcomeChannel;
            case "modchannel" -> modChannel;
            default -> "INVALID";
        };
    }

    /**
     * Resets an option based on the string provided
     * @param name - name of the option to be reset
     * @return - returns whether the function succeeded.
     */
    public String resetOptionByString(String name) {
        switch(name.toLowerCase()) {
            case "joinrole":
                joinRole = null;
                return "Auto-role on join is now set to disabled (Default).";
            case "welcomechannel":
                welcomeChannel = null;
                return "Welcome channel is now unset.";
            case "modchannel":
                modChannel = null;
                return "Mod channel is now unset.";
            default:
                return "INVALID SETTING";
        }
    }

    /**
     * Sets an option by a string, if it can find one
     * @param name - the name of the option
     * @param value - the value to have the option set to
     * @return - whether the name and value were valid and the option was set.
     */
    public String setOptionByString(String name, String value) {
        modified = true; // If this is being used set it to modified.
        switch (name.toLowerCase()) {
            case "joinrole":
                try {
                    if (Jasper.JDA.getRoleById(value) == null) {
                        return "That role ID is invalid.";
                    } else {
                        joinRole = value;
                        return "Successfully set joinRole ID to " + value;
                    }
                } catch (Exception ex) {
                    return "Bad Value";
                }
            case "welcomechannel":
                try {
                    if (Jasper.JDA.getTextChannelById(value) == null) {
                        return "That channel ID is invalid.";
                    } else {
                        welcomeChannel = value;
                        return "Successfully set welcomeChannel ID to " + value;
                    }
                } catch (Exception ex) {
                    return "Bad Value";
                }
            case "modchannel":
                try {
                    if(Jasper.JDA.getRoleById(value) == null) {
                        return "That role ID is invalid.";
                    } else {
                        modChannel = value;
                        return "Successfully set modChannel ID to " + modChannel;
                    }
                } catch (Exception ex) {
                    return "Bad Value";
                }
            default:
                return "INVALID setting name.";
        }
    }

    @Override
    public String toString() {
        return "Server [id=" + this.id + ",welcomeChannel="+welcomeChannel+
                ",joinrole=" + joinRole + ",modChannel=" + modChannel +"]";
    }
}
