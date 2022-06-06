package link.alpine.Jasper.model;

import link.alpine.Jasper.Jasper;
import net.dv8tion.jda.api.entities.Guild;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static link.alpine.Jasper.util.LoggingManager.*;

/**
 * ServerManager Class
 * Permits the access of servers easily
 * @author Laika
 */
public class ServerManager {
    //Server Memory Storage Hashmap, <Server/Guild ID, Server Object>
    private HashMap<String, Server> servers = new HashMap<>();

    /**
     * Constructor, loads servers and initializes the hashmap.
     */
    public ServerManager() {
        List<Server> loadedServers = Jasper.instance.database.loadServerInformation();
        if(loadedServers == null) {
            error("Failed to load servers properly. Null val on database.");
            return;
        }
        HashMap<String, Server> serverHashMap = new HashMap<>();
        for(Server s : loadedServers) {
            serverHashMap.put(s.getId(), s);
        }
        info("Successfully loaded " + serverHashMap.size() + " servers.");
        servers = serverHashMap;
    }

    /**
     * Fetches the server via the guild id
     * @param id - the id to find a server for
     * @return - the server, if non-existent a default profile is created for it.
     */
    public Server getOrCreateServer(String id) {
        if(servers.get(id) == null) { createNewDefaultServer(Jasper.JDA.getGuildById(id)); }
        return servers.get(id);
    }

    /**
     * Fetches the server via the guild.
     * @param guild - the guild to find the server for
     * @return - the server, if non-existent a default profile is created for it.
     */
    public Server getOrCreateServer(Guild guild) {
        if(servers.get(guild.getId()) == null) { createNewDefaultServer(guild); }
        return servers.get(guild.getId());
    }

    /**
     * Initializes a persistent profile for the server and creates a new one that is loaded into memory.
     * @param guild - the guild to have a default profile created for it.
     * @return - whether the function succeeded.
     */
    public boolean createNewDefaultServer(Guild guild) {
        var serverName = guild.getName() + ":" + guild.getId() + "].";
        debug("Creating data for [" + serverName);
        Server server = new Server(guild.getId());
        if(!Jasper.instance.getDatabase().createServerInformation(guild)) {
            error("Failed to create new defaults for " + guild.getId());
            return false;
        }
        servers.put(server.getId(), server);
        return true;
    }

    /**
     * Returns all the servers loaded in memory. Used for database persistence.
     * @return - the servers loaded in memory in a collection.
     */
    public Collection<Server> getServers() { return servers.values(); }
}
