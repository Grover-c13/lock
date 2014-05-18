package org.icannt.grover.locker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class LockManager {
	private HashMap<Short, Lock> cache;
	private Main plugin;
	protected Connection database;
	
	public LockManager(Main plugin)
	{
		this.plugin = plugin;
		this.database = plugin.database;
		cache = new HashMap<Short, Lock>();
	}
	
	
	public Main getPlugin()
	{
		return plugin;
	}
	
	// clear lock cache
	public void clearCache()
	{
		cache.clear();
	}
	
	
	public void cleanCache()
	{
		
		for(Entry<Short, Lock> entry: cache.entrySet())
		{
			if( (System.currentTimeMillis()-entry.getValue().getAccessTime()) > 600000) // 10 minutes
			{
				cache.remove(entry.getKey());
			}
		}
	}
	
	private void addCache(Lock lock)
	{
		cache.put((short) lock.getId(), lock);
	}
	

	private Lock checkCache(long id)
	{
		if ( !cache.containsKey(id) ) return null;
		Lock lock = cache.get(id);
		lock.resetAccessTime();
		return lock;
	}
	
	
	public Lock getLock(Player owner, String name)
	{
		try {
			PreparedStatement state = plugin.database.prepareStatement("SELECT * FROM `lock` WHERE `owner`='" + owner.getUniqueId().toString() + "' AND `name`='" + name + "';");
			ResultSet result = state.executeQuery();
			
			
			if (result.next())
			{
				Lock lock =  new Lock(result.getInt("id"), name, result.getString("owner"), result.getString("owner_name"), this);
				addCache(lock);
				state.close();
				result.close();
				return lock;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	// create a new lock with owner
	public Lock createLock(Player owner, String name)
	{
		Lock lock = getLock(owner, name);
		if (lock != null) return lock;
		try {
			PreparedStatement state = plugin.database.prepareStatement("INSERT INTO `lock` (owner, owner_name, name) VALUES (?, ?, ?);");
			state.setString(1, owner.getUniqueId().toString());
			state.setString(2, owner.getDisplayName());
			state.setString(3, name);
			int result = state.executeUpdate();
			if (result != 0)
			{
				int id = state.getGeneratedKeys().getInt(1);
				lock =  new Lock(id, name, owner.getUniqueId().toString(), owner.getDisplayName(), this, false);
				addCache(lock);
				state.close();
				return lock;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	// getLock from id
	public Lock getLock(int id)
	{
		Lock lock =  checkCache(id);
		if (lock != null) { return lock; }
		try {
			PreparedStatement state = plugin.database.prepareStatement("SELECT * FROM `lock` WHERE `id`=" + id + ";");
			ResultSet result = state.executeQuery();
			
			
			if (result.next())
			{
				lock =  new Lock(id, result.getString("name"), result.getString("owner"), result.getString("owner_name"), this);
				addCache(lock);
				state.close();
				result.close();
				return lock;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	

	
	
	public Lock getLockAtBlock(Block block)
	{
		return getLockAt(block.getLocation(), block.getType());
	}
	
	public Lock getLockAt(Location location, Material type)
	{
		PreparedStatement state;
		try {
			state = plugin.database.prepareStatement("SELECT `id`,`lockFK`,`type` FROM `block` WHERE `world`=? AND `x`=? AND `y`=? AND `z`=?;");
			state.setString(1, location.getWorld().getName());
			state.setLong(2, location.getBlockX());
			state.setLong(3, location.getBlockY());
			state.setLong(4, location.getBlockZ());
			ResultSet result = state.executeQuery();
			
			if (result.next())
			{
				if (!type.toString().equals(result.getString("type")))
				{
					deleteBlockLock(result.getInt("id"));
				}
				int fk = result.getInt("lockFK");
				state.close();
				result.close();
				return getLock(fk);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

		

		return null;
	}
	
	
	public void removeLockAtBlocks(List<Block> blocks)
	{
		for (Block block : blocks)
		{
			removeLockAtBlock(block);
		}
	}
	
	public void removeLockAtBlock(Block block)
	{
		PreparedStatement state;
		try {
			state = plugin.database.prepareStatement("DELETE FROM `block` WHERE `world`=? AND `x`=? AND `y`=? AND `z`=?;");
			state.setString(1, block.getLocation().getWorld().getName());
			state.setLong(2, block.getLocation().getBlockX());
			state.setLong(3, block.getLocation().getBlockY());
			state.setLong(4, block.getLocation().getBlockZ());
			state.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	protected void deleteBlockLock(int id) throws SQLException
	{
		PreparedStatement state = plugin.database.prepareStatement("DELETE FROM `block` WHERE `id`=?;");
		state.setInt(1, id);
		state.executeQuery();
		state.close();
	}
	
	
	public boolean isKey(ItemStack item)
	{
		if (item == null) return false;
		if (item.getItemMeta() == null)  return false; 
		if (item.getItemMeta().getDisplayName() == null) return false; 
		if (!item.getItemMeta().getDisplayName().substring(0, 3).equals("Key")) return false; 
		return true;
	}
	
	
}
