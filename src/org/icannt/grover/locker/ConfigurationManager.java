package org.icannt.grover.locker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigurationManager 
{
	
	private FileConfiguration config;
	private File file;
	
	private String sql;
	private List<Material> mats;
	private boolean beaconMap;
	private Material key;

	protected String mysql_user;
	protected String mysql_pass;
	protected String mysql_host;
	protected String mysql_database;
	
	public ConfigurationManager(FileConfiguration config, File file)
	{
		this.file = file;
		this.config = config;
		sql = config.getString("sql", "lite").toLowerCase();
		
		List<String> materials = config.getStringList("lockable");
		mats = new ArrayList<Material>();
		if (materials.size() > 0)
		{
			for(String mat : materials)
			{
				mats.add(Material.valueOf(mat));
			}
		} else {
			mats.add(Material.CHEST);
			mats.add(Material.TRAP_DOOR);
			mats.add(Material.TRAPPED_CHEST);
			mats.add(Material.WOODEN_DOOR);
			mats.add(Material.IRON_DOOR_BLOCK);
			mats.add(Material.FURNACE);
			mats.add(Material.DISPENSER);
			mats.add(Material.DROPPER);
			mats.add(Material.HOPPER);
			mats.add(Material.BREWING_STAND);
			mats.add(Material.BEACON);
		}
		
		key = Material.valueOf(config.getString("keyitem", "WATCH"));
		beaconMap = config.getBoolean("advanced_beacon", true);
		
		if (isMySQL())
		{
			this.mysql_user = config.getString("mysql.user", "root");
			this.mysql_pass = config.getString("mysql.pass", "");
			this.mysql_host = config.getString("mysql.host", "localhost");
			this.mysql_database = config.getString("mysql.database", "lock");
		}
	}
	
	protected void generateDefault()
	{
		config.set("sql", "lite");
		config.set("keyitem", "WATCH");
		config.set("advanced_beacon", true);
		config.set("mysql.user", "root");
		config.set("mysql.pass", "");
		config.set("mysql.host", "localhost");
		config.set("mysql.database", "lock");
		config.set("lockable", matsToString());
		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	public String getBackend()
	{
		return sql;
	}
	
	public boolean isLite()
	{
		return (sql.equals("sqllite") || sql.equals("lite"));
	}
	
	public boolean isMySQL()
	{
		return (sql.equals("mysql") || sql.equals("my"));
	}
	
	public List<Material> getLockables()
	{
		return this.mats;
	}
	
	public boolean isLockable(Material mat)
	{
		return mats.contains(mat);
	}
	
	public boolean doBeaconMap()
	{
		return this.beaconMap;
	}
	
	public List<String> matsToString()
	{
		List<String> strings = new ArrayList<String>();
		for(Material mat : mats)
		{
			strings.add(mat.toString());
		}
		
		return strings;
	}
	
	public Material getKey()
	{
		return this.key;
	}
}
