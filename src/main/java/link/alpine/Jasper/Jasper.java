package link.alpine.Jasper;

import link.alpine.Jasper.command.CommandClass;
import link.alpine.Jasper.command.CommandRegistrar;
import link.alpine.Jasper.model.JasperDB;
import link.alpine.Jasper.model.ServerManager;
import link.alpine.Jasper.util.CommandInfo;
import link.alpine.Jasper.util.EmbedUI;
import link.alpine.Jasper.util.LoggingManager;
import link.alpine.Jasper.listener.MainListener;
import link.alpine.Jasper.listener.SkynetListener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.simpleyaml.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Jasper Main Class
 * @author Laika
 * @version 1.0.0-rel
 * @apiNote I wrote 1900 lines of code, opposed the license agreement and i own the bot :^)
 * IF you have any complaints, (and you know who you are) please remind yourself that you literally
 * only wrote logger utilities, because you did not have enough competence to do anything else.
 */
public class Jasper {

    private final File CONFIG_FILE = new File("config.yml");

    public YamlConfiguration yamlConfiguration = new YamlConfiguration();

    public String footer = EmbedUI.BRAND + "1.0.0-rel";

    public String name = "Jasper";

    public static Jasper instance;

    public static JDA JDA;

    public List<CommandClass> activeCommands;

    public JasperConfig config;

    public JasperDB database;
    public ServerManager serverManager;

    /**
     * Main Class function.
     * @param args - Arguments for program start.
     */
    public static void main(String[] args) {
        try {
            var jas = new Jasper();
            jas.start();
        } catch (Exception ex) {
            System.out.println("Failed to start Jasper, check your Java installation.");
            ex.printStackTrace();
        }
    }

    /**
     * Ran on program start. Anything in here can determine whether the program will start.
     */
    public void start() {
        instance = this;
        LoggingManager.info("Starting Jasper.");

        // All commands to be loaded on startup!
        activeCommands = new CommandRegistrar().getCommandClasses();

        // Ensuring the configuration file is generated and/or exists.
        if (!CONFIG_FILE.exists()) {
            try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("config.yml")) {
                // Save the default config
                Files.copy(is, CONFIG_FILE.toPath());
            } catch (Exception ex) {
                LoggingManager.warn("Failed to create the configuration file. Stopping. (" + CONFIG_FILE.getAbsolutePath() + ")");
                ex.printStackTrace();
                return;
            }
        }

        // Try to load configuration into the configuration API.
        try {
            yamlConfiguration.load("config.yml");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            LoggingManager.warn("File not found, must've failed to create...");
        } catch (Exception e) {
            LoggingManager.warn("Ensure all values are inputted properly.");
            e.printStackTrace();
        }

        // Initializes our configuration helper & ensures it loads properly.
        config = new JasperConfig(yamlConfiguration);
        if(config.load()) {
            LoggingManager.info("Fetched Jasper config.");
        } else {
            LoggingManager.error("Failed to load configuration. Stopping process.");
            Runtime.getRuntime().exit(0);
        }

        // Registers the stop() function if the program is stopped.
        LoggingManager.info("Registering shutdown hook.");
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

        // Initializes database and loads credentials.
        database = config.createDb();

        serverManager = new ServerManager();

        // Makes our JDA instance.
        startDiscord();
    }

    /**
     * Ran on program shutdown.
     */
    public void stop() {
        var build = new EmbedBuilder()
                .setColor(EmbedUI.FAILURE)
                .setTitle("Offline")
                .setFooter(footer)
                .setTimestamp(ZonedDateTime.now());
        JDA.getTextChannelById(config.getLogChannel()).sendMessageEmbeds(build.build()).queue();
        LoggingManager.info("Shutting down Jasper.");
        if(database.saveServerInformation()) {
            LoggingManager.info("Successfully saved server information. Shutting down peacefully.");
        } else {
            for(int i = 0; i < 15; i++) {
                LoggingManager.error("FAILED TO SAVE SERVER INFORMATION.");
            }
        }
    }

    /**
     * Starts a JDA Instance.
     */
    public void startDiscord() {
        try {
            JDA = JDABuilder.create(config.getToken(), GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_VOICE_STATES)
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .disableCache(CacheFlag.EMOTE)
                    .setActivity(Activity.of(Activity.ActivityType.valueOf(config.getActivityType()), config.getActivityMsg()))
                    .setStatus(OnlineStatus.valueOf(config.getStatusType()))
                    .addEventListeners(
                            new MainListener(),
                            new SkynetListener()
                    ).build().awaitReady();
        } catch(Exception ex) {
            LoggingManager.error("Initialization broke...");
            ex.printStackTrace();
            return;
        }

        registerAllCommands();
        LoggingManager.info("Finished registering commands.");

        var eb = new EmbedBuilder()
                .setColor(EmbedUI.SUCCESS)
                .setTitle("Online")
                .setFooter(footer)
                .setTimestamp(ZonedDateTime.now());
        JDA.getTextChannelById(config.getLogChannel()).sendMessageEmbeds(eb.build()).queue();
    }

    /**
     * Quick method to register commands in all servers.
     */
    private void registerAllCommands() {
        // Registers slash commands.
        LoggingManager.info("Registering commands for guilds.");
        int i = 0;
        for(Guild guild : JDA.getGuilds()) {
            registerForGuild(guild);
            i++;
        }
        LoggingManager.info("Registered commands for " + i + " guilds.");

        // Registers the event listeners for those commands.
        for(CommandClass cmd : activeCommands) {
            JDA.addEventListener(cmd);
        }
    }

    /**
     * Registers all bot commands with the guild provided
     * @param guild - guild to have commands provided to
     */
    public void registerForGuild(Guild guild) {
        LoggingManager.debug("Registering commands for guild [" + guild.getName() + ":" + guild.getId() + "]");
        int i = 0;
        int j = 0;
        for(CommandClass cmd : activeCommands) {
            for(CommandInfo ci : cmd.getSlashCommandInfo()) {
                var cca = guild.upsertCommand(ci.getName(), ci.getDescription());
                i++;
                if(ci.hasSubCommands()) {
                    for (String name : ci.getSubCommands().keySet()) {
                        var si = ci.getSubCommands().get(name);
                        var sd = new SubcommandData(si.getName(), si.getDescription());
                        for (String option : si.getOptions().keySet()) {
                            sd.addOption(si.getOptions().get(option), option, si.getOptionDescriptions().get(option), si.getOptionRequirements().get(option));
                        }
                        cca.addSubcommands(sd);
                    }
                }
                if(ci.hasOptions()) {
                    for(String name : ci.getOptions().keySet()) {
                        // Any intelligent IDE will rage about the option not being used, it's added to the action then executed later, don't edit without reason.
                        cca.addOption(ci.getOptions().get(name), name, ci.getOptionDescriptions().get(name), ci.getOptionRequirements().get(name));
                    }
                }
                // Push w/ modifications.
                //trace("Command: " + ci.getName() + " registration on " + guild.getId() + " completed.");
                try {
                    cca.queue();
                } catch (Exception ex) {
                    // Only time this should occur is when a server does not have the proper scope.
                    LoggingManager.error("Failed to queue command for RestAction.");
                }
            }
        }
        LoggingManager.debug("Registered " + i + " commands for guild [" + guild.getId() + "]");
    }

    // Gets the active database.
    public JasperDB getDatabase() { return database; }

    // Gets active ServerManager
    public ServerManager getServerManager() { return serverManager; }
}