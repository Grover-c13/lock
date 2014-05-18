package org.icannt.grover.locker;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;



public class Lock {
	
	private HashMap<String, LockRecord> perms;
	private int id;
	private String owner;
	private LockManager manager;
	private Long lastAccess;
	private String ownername;
	
	public Lock(int id, String name, String owner, String ownername, LockManager manager)
	{
		this(id, name, owner, ownername, manager, true);
	}
	
	public Lock(int id, String name, String owner, String ownername, LockManager manager, boolean load)
	{
		this.manager = manager;
		this.owner = owner;
		this.ownername = ownername;
		this.id = id;
		this.perms = new HashMap<String, LockRecord>();
		this.lastAccess = System.currentTimeMillis();
		if (load) this.loadRecords();
		
	}
	
	
	public boolean equals(Lock lock)
	{
		return (this.id == lock.id);
	}
	
	public void addRecord(Player player, int level)
	{
		String uuid = player.getUniqueId().toString();

		try {
			PreparedStatement state = manager.database.prepareStatement("INSERT INTO `lock_record` (lockFK, type, value, level) VALUES (?, 'player', ?, ?);");
			state.setInt(1, this.id);
			state.setString(2, uuid);
			state.setInt(3, level);
			state.execute();
			LockRecord record = new LockRecord(this, uuid, level);
			perms.put(uuid, record);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
				
	}
	
	public void remove()
	{
		try {
			PreparedStatement state = manager.database.prepareStatement("DELETE FROM `locks` WHERE `id` = ?;");
			state.setInt(1, this.id);
			state.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
	}
	
	public boolean canPlayerUse(Player player)
	{
		if (player.getUniqueId().toString().equals(owner)) return true;
		return this.perms.containsKey(player.getUniqueId().toString());
	}
	
	public boolean canPlayerEditUsers(Player player)
	{
		if (player.getUniqueId().toString().equals(owner)) return true;
		if (canPlayerUse(player))
		{
			if( this.perms.get(player.getUniqueId().toString()).getLevel() > 0 ) 
			{
				return true;
			}
		}
		
		return false;
	}
	
	public String removeUser(Player caller, Player target)
	{
		if (!canPlayerEditUsers(caller)) return "You do not have permissions to remove users from a lock";
		if (target.getUniqueId().toString().equals(this.owner))  return "You cant remove the owner from the lock";
		if (!canPlayerUse(target)) return "User is not allowed to use this lock anyway";
		String key = target.getUniqueId().toString();
		this.perms.remove(key);
		

		try {
			PreparedStatement state = manager.database.prepareStatement("DELETE FROM `lock_record` WHERE `lockFK`="  + this.id + " AND `player`='" + key + "';");
			if (state.executeUpdate() > 0)
			{
				state.close();
				return "User removed";
			}
			state.close();
		} catch (SQLException e) {
			System.out.println(e.getSQLState());
			e.printStackTrace();
		}
	
		
		
		return "User failed to be removed";
	}
	
	public Player getOwner()
	{
		return manager.getPlugin().getServer().getPlayer(UUID.fromString(this.owner));
	}
	
	protected String getOwnerString()
	{
		return owner;
	}
	
	protected void loadRecords()
	{
		try {
			PreparedStatement state = manager.database.prepareStatement("SELECT * FROM `lock_record` WHERE `lockFK`='"  + this.id + "';");
			ResultSet result = state.executeQuery();
			
			while(result.next())
			{
				LockRecord record = new LockRecord(this, result.getString("value"), result.getInt("level"));
				this.perms.put(result.getString("value"), record);
			}
			
			result.close();
			state.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
				
		
	
	}
	
	
	public int getId()
	{
		return this.id;
	}
	
	
	public void applyLockToBlocks(List<Block> blocks)
	{
		for (Block block : blocks)
		{
			applyLockToBlock(block);
		}
	}
	
	public void applyLockToBlock(Block block)
	{
		Lock current = manager.getLockAtBlock(block);
		if (current != null)
		{
			if (current.getId() == this.getId()) return;
		}
		
		PreparedStatement state;
		try {
			state = manager.database.prepareStatement("INSERT INTO `block` (lockFK, world, x, y, z, type) VALUES (?, ?, ?, ?, ?, ?);");
			state.setInt(1, this.id);
			state.setString(2, block.getLocation().getWorld().getName());
			state.setLong(3, block.getLocation().getBlockX());
			state.setLong(4, block.getLocation().getBlockY());
			state.setLong(5, block.getLocation().getBlockZ());
			state.setString(6, block.getType().toString());
			state.executeUpdate();
			state.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		
	}
	
	public String getOwnerName()
	{
		return this.ownername;
	}
	
	
	public Long getAccessTime()
	{
		return this.lastAccess;
	}
	
	public List<String> generateItemlore()
	{
		List<String> list = new ArrayList<String>();
		list.add(ChatColor.GOLD + "Owner: " + ChatColor.GREEN + getOwnerName());
		
		for(Entry<String, LockRecord> entry : this.perms.entrySet())
		{
			if(entry.getValue().getLevel() > 0)
			{
				list.add(ChatColor.GREEN + entry.getValue().recordAsPlayer().getDisplayName());
				continue;
			}
			
			list.add(ChatColor.WHITE + entry.getValue().recordAsPlayer().getDisplayName());
		}
		
		return list;
	}
	
	public void resetAccessTime()
	{
		this.lastAccess = System.currentTimeMillis();
	
	}
}
