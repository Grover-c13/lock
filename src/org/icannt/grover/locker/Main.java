package org.icannt.grover.locker;


import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;




public class Main extends JavaPlugin {
	protected Connection database;
	protected LockManager manager;
	protected ConfigurationManager config;
	
	public void onEnable()
	{
		
		
		try {
			
			File folder = this.getDataFolder();
			if (!folder.exists())
			{
				folder.mkdir();
			}
			
			File configFile = new File(folder + "/config.yml");
			config = new ConfigurationManager(this.getConfig(), configFile);
			if (!configFile.exists())
			{
				config.generateDefault();
			}
			
		
			
			if (config.isLite()) database = createLiteConnection();
			if (config.isMySQL()) database = createMysqlConnection();
			
			manager = new LockManager(this);
		
			setupTables();
			this.getCommand("key").setExecutor(new KeyCommand(this));
			this.getCommand("lock").setExecutor(new KeyCommand(this));
			this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
			
			// clean cache
            Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
                public void run() {
                	manager.cleanCache();
                }
            }, 30000, 30000);
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void onDisable()
	{
		try {
			database.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private Connection createMysqlConnection() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException
	{
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		return DriverManager.getConnection("jdbc:mysql://" + config.mysql_host + "/" + config.mysql_database, config.mysql_user, config.mysql_pass);
	}
	
	
	private Connection createLiteConnection() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException
	{
		Class.forName("org.sqlite.JDBC").newInstance();
		return DriverManager.getConnection("jdbc:sqlite:" + new File(this.getDataFolder() + "/database.db").toPath(), "", "");
	}
	
	private void setupTables() throws SQLException
	{
		database.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS `lock` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `owner` varchar(36) NOT NULL, `owner_name` varchar(30), `name` varchar(50) NOT NULL)");
		database.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS `lock_record` (`lockFK` INTEGER, `type` varchar(30), `value` varchar(36) NOT NULL, `level` INT(1))");
		database.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS `block` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `lockFK` INTEGER, `world` VARCHAR(30), `x` LONG NOT NULL,  `Y` LONG NOT NULL,  `Z` LONG NOT NULL, `type` VARCHAR(30))");
	}
	
	
	@SuppressWarnings("deprecation")
	public Player getPlayer(String name)
	{
		
		Server server = Bukkit.getServer();
		Player player = server.getPlayer(name);
		
		if (player == null)
		{
			player = server.getOfflinePlayer(name).getPlayer();
		}
		
		return player;
	
	}

	
	

}
