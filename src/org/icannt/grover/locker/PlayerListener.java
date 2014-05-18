package org.icannt.grover.locker;



import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {
	private Main plugin;
	public PlayerListener(Main plugin)
	{
		this.plugin = plugin;
	}
	
	

	@EventHandler(priority=EventPriority.MONITOR)
	public void sign(SignChangeEvent event)
	{
		String line = event.getLine(0);
		if (line == null) return;
		if (line.toLowerCase().equals("[private]"))
		{
			event.getPlayer().sendMessage(ChatColor.GOLD + "This server does not run Lockette/DeadBolt, please use \"/key help\" for information on the locking system.");
		}
	}
	
	@EventHandler(priority=EventPriority.LOW)
	public void breakBlock(BlockBreakEvent event)
	{
		if (!plugin.config.isLockable(event.getBlock().getType())) return;
		Lock lock = plugin.manager.getLockAtBlock(event.getBlock());
		if (lock != null)
		{
			if (lock.canPlayerEditUsers(event.getPlayer()) || event.getPlayer().hasPermission("lock.bypass.destroy"))
			{
				List<Block> mapped = BlockMapper.map(event.getBlock(), plugin);
				
				if (mapped == null)
				{
					plugin.manager.removeLockAtBlock(event.getBlock());
				} else {
					plugin.manager.removeLockAtBlocks(mapped);
				}
				
				event.getPlayer().sendMessage(ChatColor.RED + "Lock destroyed on block!");
			} else 
			{
				event.setCancelled(true);
				event.getPlayer().sendMessage(ChatColor.RED + "You can not break this block because it is locked!");
			}
				
		}
		
	}
	
	@EventHandler(priority=EventPriority.LOW)
	public void interact(PlayerInteractEvent event)
	{ 

		if ( !(event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) ) return;
		if (!plugin.config.isLockable(event.getClickedBlock().getType())) return;
		ItemStack item = event.getPlayer().getItemInHand();
		if (plugin.manager.isKey(item))
		{
			Lock lock = plugin.manager.getLock(item.getDurability());
			
			if (!lock.canPlayerUse(event.getPlayer()) )
			{
				event.getPlayer().sendMessage(ChatColor.RED + "You do not have permission to apply this lock!");
				event.setCancelled(true);
				return;
			}
			
			Lock exist = plugin.manager.getLockAtBlock(event.getClickedBlock());
			if (exist != null)
			{
				if (exist.equals(lock) || event.getPlayer().hasPermission("lock.bypass.remove"))
				{
					
					
					List<Block> mapped = BlockMapper.map(event.getClickedBlock(), plugin);
					
					if (mapped == null)
					{
						plugin.manager.removeLockAtBlock(event.getClickedBlock());
					} else {
						plugin.manager.removeLockAtBlocks(mapped);
					}
					
					event.getPlayer().sendMessage(ChatColor.GOLD + "Lock removed from this block!");
					event.setCancelled(true);
					return;
					
					
				}
			}
			

			
			if (plugin.manager.getLockAtBlock(event.getClickedBlock()) == null) 
			{
				
				List<Block> mapped = BlockMapper.map(event.getClickedBlock(), plugin);
				
				if (mapped == null)
				{
					lock.applyLockToBlock(event.getClickedBlock());
				} else {
					lock.applyLockToBlocks(mapped);
				}
				
				event.getPlayer().sendMessage(ChatColor.GOLD + "Block locked!");
				event.setCancelled(true);
				return;
				
			}
		}
		
		
		
		Lock lock = plugin.manager.getLockAtBlock(event.getClickedBlock());
		if (lock != null)
		{
			boolean canUse = lock.canPlayerUse(event.getPlayer());
			if (!canUse || !event.getPlayer().hasPermission("lock.bypass.use"))
			{
				event.setCancelled(true);
				event.getPlayer().sendMessage(ChatColor.RED + "locked!");
			}

			return;
		}
	

		
	}
	


}
