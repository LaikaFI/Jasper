package link.alpinia.Jasper.model;

import link.alpinia.Jasper.util.LoggingManager;
import link.alpinia.Jasper.Jasper;
import net.dv8tion.jda.api.entities.Guild;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Jasper DB Class
 * Basically our helpful MySQL functions that pertain to data persistence.
 * @author Jasper
 * @apiNote SQL is perpetually horrid. PostgreSQL is on the table, someday.
 */
public class JasperDB {
    // Our actual MySQL Connection, this is created on class construction.
    private Connection connection;

    // The prepared statement strings
    private final String CREATE_SERVERINFO_TABLE = "CREATE TABLE IF NOT EXISTS `serverInfo`" +
            "(`id` LONGTEXT NOT NULL, `welcomeChannel` LONGTEXT NULL, `modChannel` LONGTEXT NULL, joinRole LONGTEXT NULL);";
    private final String INSERT_SERVERINFO_DEFAULT = "INSERT INTO serverInfo(id, welcomeChannel, modChannel, joinRole) values (?,?,?,?)";
    private final String SERVERINFO_EXISTS = "select * from serverInfo where id = ?";
    private final String SERVER_INFO_LOAD = "select * from serverInfo";
    // 1 = welcomeChannel, 2 = modChannel , 3 = joinRole, 4 = Guild_ID (immutable)
    private final String SERVER_INFO_MODIFY = "UPDATE `serverInfo` SET welcomeChannel=?, modChannel=?, joinRole=? WHERE id = ?";

    /**
     * JasperDB Config Constructor
     * @param username - username to access database
     * @param password - password to access database
     * @param host - host for server
     * @param port - port for server (3306 is default)
     * @param db - database name
     */
    public JasperDB(String username, String password, String host, int port, String db) {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + db, username, password);
        } catch (Exception ex) {
            LoggingManager.error("Failed to initialize the MySQL instance.");
            ex.printStackTrace();
            return;
        }

        initializeTables();
    }

    /**
     * Table initialization function, ran on every startup.
     */
    private void initializeTables() {
        try {
            connection.prepareStatement(CREATE_SERVERINFO_TABLE).execute();
        } catch (Exception ex) {
            ex.printStackTrace();
            LoggingManager.error("Failed to initialize SQL tables. They may need to be created manually.");
        }
    }

    /**
     * Creates the default server information when the bot joins a discord.
     * @param guild - the guild that the bot joined
     * @return whether the function succeeded.
     */
    public boolean createServerInformation(Guild guild) {
        try {
            PreparedStatement prep = connection.prepareStatement(SERVERINFO_EXISTS);
            prep.setInt(1, (int) guild.getIdLong());
            ResultSet rsCheck = prep.executeQuery();
            while (rsCheck.next()) {
                //Server already exists, no need to initialize a section for it.
                if(rsCheck.getInt(1) != 0) {
                    LoggingManager.info("Server already existed. Skipping initialization.");
                    return true;
                }
            }
            //Proceed with making defaults.
            PreparedStatement ps = connection.prepareStatement(INSERT_SERVERINFO_DEFAULT);
            ps.setString(1, guild.getId());
            ps.setNull(2, Types.LONGVARCHAR);
            ps.setNull(3, Types.LONGVARCHAR);
            ps.setNull(4, Types.LONGVARCHAR);
            ps.execute();
            return true;
        } catch (Exception ex) {
            LoggingManager.error("Failed to create default server info for guild " + guild.getId());
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Loads all the server information from MySQL into memory.
     * @return - a list of all servers loaded from MySQL.
     */
    public List<Server> loadServerInformation() {
        List<Server> servers = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(SERVER_INFO_LOAD);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                LoggingManager.info("Starting new load for server: " + rs.getString(1));
                String id = rs.getString(1);
                String welcomeChannel = rs.getString(2);
                String modChannel = rs.getString(3);
                String joinRole = rs.getString(4);
                Server server = new Server(id, welcomeChannel, modChannel, joinRole);
                LoggingManager.debug("Loaded " + server + "from database.");
                servers.add(server);
            }
            return servers;
        } catch (Exception ex) {
            ex.printStackTrace();
            LoggingManager.error("Failed to load server information, check stack.");
            return null;
        }
    }

    /**
     * Saves modified servers into persistent MySQL server.
     * @return - whether the method succeeded.
     */
    public boolean saveServerInformation() {
        Collection<Server> servers = Jasper.instance.getServerManager().getServers();
        LoggingManager.info("Starting save on " + servers.size() + " servers.");
        try {
            // 1 = welcomeChannel, 2 = modChannel, 3 = joinRole, 4 = Guild_ID (immutable)
            PreparedStatement ps = connection.prepareStatement(SERVER_INFO_MODIFY);
            int i = 0;
            for (Server server : servers) {
                if(!server.isModified()) { continue; } // Skip, unmodified server.
                LoggingManager.info("Starting save on modified " + server);
                if(server.getWelcomeChannel() != null) {
                    ps.setString(1, server.getWelcomeChannel());
                } else {
                    ps.setNull(1, Types.LONGVARCHAR);
                }
                if(server.getModChannel() != null) {
                    ps.setString(2, server.getModChannel());
                } else {
                    ps.setNull(2, Types.LONGVARCHAR);
                }
                if(server.getJoinRole() != null) {
                    ps.setString(3, server.getJoinRole());
                } else {
                    ps.setNull(3, Types.LONGVARCHAR);
                }
                ps.setString(4, server.getId());
                ps.addBatch();
                i++;
            }
            LoggingManager.info("Total Batches: " + i + ". Starting SQL save.");
            ps.executeBatch();
            return true;
        } catch (Exception ex) {
            LoggingManager.error("Failed to persist server data. Check stack.");
            ex.printStackTrace();
            return false;
        }
    }
}
