package org.icannt.grover.locker;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;


public class LockRecord {
	private Lock lock;
	private int level = 0;
	private String record;
	
	public LockRecord(Lock lock, String record, int level)
	{
		this.record = record;
		this.level = level;
		this.lock = lock;
	}
	
	
	public int getLevel()
	{
		return level;
	}
	
	public String getRecord()
	{
		return record;
	}
	
	public Lock getLock()
	{
		return lock;
	}
	
	public Player recordAsPlayer()
	{
		
		Server server = Bukkit.getServer();
		UUID uuid = UUID.fromString(record);
		Player player = server.getPlayer(uuid);
		
		if (player == null)
		{
			player = server.getOfflinePlayer(uuid).getPlayer();
		}
		
		return player;
	
	}
}
