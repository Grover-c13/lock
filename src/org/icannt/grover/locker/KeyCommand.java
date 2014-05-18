package org.icannt.grover.locker;



import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class KeyCommand implements CommandExecutor {
	
	Main plugin;
	public KeyCommand(Main plugin)
	{
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) 
	{
		
		
		if (args.length == 1)
		{
			
			if (args[0].equals("help"))
			{
				
				sender.sendMessage(ChatColor.GOLD + "Type /key create <NAME> to create a key to lock things with.");
				sender.sendMessage(ChatColor.GOLD + "Right click on lockable blocks with this key to lock them, right click again to remove them.");
				sender.sendMessage(ChatColor.GOLD + "Type /key add <PLAYER> to add a player to a key, this applies to all locked objects.");
				sender.sendMessage(ChatColor.GOLD + "Type /key manager <PLAYER> to add a manager to the key, managers can do everything except remove owners.");
				sender.sendMessage(ChatColor.GOLD + "This key can be recreated at anytime by typine /key create <NAME> again!");
				return true;
				
			}
			
		}
		
		if (args.length == 2)
		{
			
			if (args[0].equals("create"))
			{
				if ( !sender.hasPermission("key.create") ) { sender.sendMessage(ChatColor.RED + "No permission."); return true; }
				if (!(sender instanceof Player)) return false;
				Player player = (Player) sender;
				Lock lock = plugin.manager.createLock(player, args[1]);
				ItemStack item = new ItemStack(plugin.config.getKey(), 1);
				ItemMeta meta = item.getItemMeta();

				meta.setDisplayName("Key (" + args[1] + ")");
				item.setDurability( (short) lock.getId());
				meta.setLore(lock.generateItemlore());
				
				item.setItemMeta(meta);
				player.getInventory().addItem(item);
				return true;
			}
			
			
			
			if (args[0].equals("add"))
			{
				if ( !sender.hasPermission("key.add") ) { sender.sendMessage(ChatColor.RED + "No permission."); return true; }
				// player check
				if (!(sender instanceof Player)) { sender.sendMessage(ChatColor.RED + "Must be player"); return true; }
				Player player = (Player) sender;
				
				// target check
				Player target = plugin.getPlayer(args[1]);
				if (target == null)  { sender.sendMessage(ChatColor.RED + "Invalid target"); return true; }
				
				// key check
				ItemStack item = player.getItemInHand();
				if (!plugin.manager.isKey(item)) { sender.sendMessage(ChatColor.RED + "Must be holding a valid key"); return true; }
				
				// get lock
				Lock lock = plugin.manager.getLock((int) item.getDurability());
				
				if (lock.canPlayerUse(target)) { cmd.setUsage(ChatColor.RED + "Player already in this lock group!"); return false; }
				if (!lock.canPlayerEditUsers(player)) { cmd.setUsage(ChatColor.RED + "You do not have permission to edit users"); return false; }
				
				lock.addRecord(target, 0);
				
				ItemMeta meta = item.getItemMeta();
				meta.setLore(lock.generateItemlore());
				item.setItemMeta(meta);
				sender.sendMessage(ChatColor.GOLD + target.getName() + " Added!");
				return true;
			}
			
			
			if (args[0].equals("manager"))
			{
				if ( !sender.hasPermission("key.manager") ) { sender.sendMessage(ChatColor.RED + "No permission."); return true; }
				// player check
				if (!(sender instanceof Player)) { sender.sendMessage(ChatColor.RED + "Must be player"); return true; }
				Player player = (Player) sender;
				
				// target check
				Player target = plugin.getPlayer(args[1]);
				if (target == null)  { sender.sendMessage(ChatColor.RED + "Invalid target"); return true; }
				
				// key check
				ItemStack item = player.getItemInHand();
				if (!plugin.manager.isKey(item)) { sender.sendMessage(ChatColor.RED + "Must be holding a valid key"); return true; }
				
				// get lock
				Lock lock = plugin.manager.getLock((int) item.getDurability());
				
				if (!lock.canPlayerEditUsers(player)) { cmd.setUsage(ChatColor.RED + "You do not have permission to edit users"); return false; }
				
				lock.addRecord(target, 1);
				
				ItemMeta meta = item.getItemMeta();
				meta.setLore(lock.generateItemlore());
				item.setItemMeta(meta);
				sender.sendMessage(ChatColor.GOLD + target.getName() + " Added!");
				return true;
			}
			
			
			if (args[0].equals("remove"))
			{
				if ( !sender.hasPermission("key.remove") ) { sender.sendMessage(ChatColor.RED + "No permission."); return true; }
				// player check
				if (!(sender instanceof Player)) { sender.sendMessage(ChatColor.RED + "Must be player"); return true; }
				Player player = (Player) sender;
				
				// target check
				Player target = plugin.getPlayer(args[1]);
				if (target == null)  { sender.sendMessage(ChatColor.RED + "Invalid target"); return true; }
				
				// key check
				ItemStack item = player.getItemInHand();
				if (!plugin.manager.isKey(item)) { sender.sendMessage(ChatColor.RED + "Must be holding a valid key"); return true; }
				
				// get lock
				Lock lock = plugin.manager.getLock((int) item.getDurability());
				
				if (!lock.canPlayerEditUsers(player)) { sender.sendMessage(ChatColor.RED + "You do not have permission to edit users"); return true; }
				
				String result = lock.removeUser(player, target);
				
				ItemMeta meta = item.getItemMeta();
				meta.setLore(lock.generateItemlore());
				item.setItemMeta(meta);
				
				sender.sendMessage(result);
				return true;
			}
			
		}
		
		
		return false;
	}
	


}
