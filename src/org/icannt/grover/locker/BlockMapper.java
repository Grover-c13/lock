package org.icannt.grover.locker;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public class BlockMapper {
	
	public static List<Block> map(Block block, Main plugin)
	{
		
		if (block.getType() == Material.CHEST) return mapChest(block);
		if (block.getType() == Material.TRAPPED_CHEST) return mapTrappedChest(block);
		if (block.getType() == Material.IRON_DOOR_BLOCK) return mapIronDoor(block);
		if (block.getType() == Material.WOODEN_DOOR) return mapWoodDoor(block);
		//if (plugin.config.doBeaconMap()) if (block.getType() == Material.BEACON) return mapBeacon(block);
		return null;
		
		
	}
	
	public static List<Block> mapChest(Block block)
	{
		List<Block> blocks = new ArrayList<Block>();
		blocks.add(block);

		Location origin = block.getLocation();
		World world = block.getWorld();
		
		Block check = world.getBlockAt(origin.clone().add(1, 0, 0));
		if (check.getType().equals(Material.CHEST)) blocks.add(check);
		check = world.getBlockAt(origin.clone().add(0, 0, 1));
		if (check.getType().equals(Material.CHEST)) blocks.add(check);
		check = world.getBlockAt(origin.clone().subtract(1, 0, 0));
		if (check.getType().equals(Material.CHEST)) blocks.add(check);
		check = world.getBlockAt(origin.clone().subtract(0, 0, 1));
		if (check.getType().equals(Material.CHEST)) blocks.add(check);

		
		return blocks;
	}
	
	
	public static List<Block> mapTrappedChest(Block block)
	{
		List<Block> blocks = new ArrayList<Block>();
		blocks.add(block);

		Location origin = block.getLocation();
		World world = block.getWorld();
		
		Block check = world.getBlockAt(origin.clone().add(1, 0, 0));
		if (check.getType().equals(Material.TRAPPED_CHEST)) blocks.add(check);
		check = world.getBlockAt(origin.clone().add(0, 0, 1));
		if (check.getType().equals(Material.TRAPPED_CHEST)) blocks.add(check);
		check = world.getBlockAt(origin.clone().subtract(1, 0, 0));
		if (check.getType().equals(Material.TRAPPED_CHEST)) blocks.add(check);
		check = world.getBlockAt(origin.clone().subtract(0, 0, 1));
		if (check.getType().equals(Material.TRAPPED_CHEST)) blocks.add(check);

		
		return blocks;
	}
	
	public static List<Block> mapWoodDoor(Block block)
	{
		List<Block> blocks = new ArrayList<Block>();
		blocks.add(block);

		Location origin = block.getLocation();
		World world = block.getWorld();

		Block check = world.getBlockAt(origin.clone().add(0, 1, 0));
		if (check.getType().equals(Material.WOODEN_DOOR)) blocks.add(check);
		check = world.getBlockAt(origin.clone().subtract(0, 1, 0));
		if (check.getType().equals(Material.WOODEN_DOOR)) blocks.add(check);

		
		return blocks;
	}
	
	
	public static List<Block> mapIronDoor(Block block)
	{
		List<Block> blocks = new ArrayList<Block>();
		blocks.add(block);

		Location origin = block.getLocation();
		World world = block.getWorld();

		Block check = world.getBlockAt(origin.clone().add(0, 1, 0));
		if (check.getType().equals(Material.IRON_DOOR_BLOCK)) blocks.add(check);
		check = world.getBlockAt(origin.clone().subtract(0, 1, 0));
		if (check.getType().equals(Material.IRON_DOOR_BLOCK)) blocks.add(check);

		
		return blocks;
	}
	
	
	public static List<Block> mapBeacon(Block block)
	{
		List<Block> blocks = new ArrayList<Block>();
		List<Block> checks = null;
		blocks.add(block);

		Location origin = block.getLocation();
		World world = block.getWorld();

		// first layer
		int area = 3;
		Location corner = origin.clone().subtract(Math.round(area/2), 1, Math.round(area/2));
		checks = mapValueableBlockSquare(corner, area, world, blocks);
		if (checks == null) 
		{
			return blocks;
		} else {
			blocks = checks;
		}
		
		System.out.println("first layer done; going to next");
		
		// second layer
	
		area = 5;
		corner = origin.clone().subtract(Math.round(area/2), 2, Math.round(area/2));
		checks = mapValueableBlockSquare(corner, area, world, blocks);
		
		if (checks == null) 
		{
			return blocks;
		} else {
			blocks = checks;
		}
		
		System.out.println("second layer done; going to next");
		
		
		// third layer
		area = 7;
		corner = origin.clone().subtract(Math.round(area/2), 3, Math.round(area/2));
		checks = mapValueableBlockSquare(corner, area, world, blocks);
		if (checks == null) 
		{
			return blocks;
		} else {
			blocks = checks;
		}
		
		System.out.println("third layer done; going to next");
		
		// fourth layer
		area = 9;
		corner = origin.clone().subtract(Math.round(area/2), 4, Math.round(area/2));
		checks = mapValueableBlockSquare(corner, area, world, blocks);
		if (checks == null) 
		{
			return blocks;
		} else {
			blocks = checks;
		}
		
		
		
		return blocks;
	}
	
	public static List<Block> mapValueableBlockSquare(Location corner, int area, World world, List<Block> cont)
	{
		if (cont == null) 
		{
			cont = new ArrayList<Block>();
		}
		
		for(int x =0; x < area; x++)
		{
			for(int z =0; z < area; z++)
			{
				Block check = world.getBlockAt(corner.getBlockX()+x, corner.getBlockY(), corner.getBlockZ()+z);
				if (check.getType() == Material.IRON_BLOCK || check.getType() == Material.GOLD_BLOCK || check.getType() == Material.DIAMOND_BLOCK) { 
					cont.add(check);
					check.setType(Material.GOLD_BLOCK);
					continue; 
				} else {
					return null; // invalid, non valueable blocks used.
				}
			}
		}
		
		return cont;
	}
	
}
