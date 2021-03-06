package me.Josvth.RandomSpawn;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.Josvth.RandomSpawn.Handlers.CommandHandler;
import me.Josvth.RandomSpawn.Handlers.YamlHandler;
import me.Josvth.RandomSpawn.Listeners.*;

public class RandomSpawn extends JavaPlugin{

	Logger logger;

	public YamlHandler yamlHandler;
	CommandHandler commandHandler;

	RespawnListener respawnListener;
	JoinListener joinListener;
	SignListener signListener;
	DamageListener damageListener;
	
	@Override
	public void onEnable() {

		logger = Logger.getLogger("Minecraft");

		//setup handlers
		yamlHandler = new YamlHandler(this);
		logDebug("Yamls loaded!");

		commandHandler = new CommandHandler(this);
		logDebug("Commands registered!");

		//setup listeners
		respawnListener = new RespawnListener(this);
		joinListener = new JoinListener(this);
		signListener = new SignListener(this);
		damageListener = new DamageListener(this);
		
	}

	public void logInfo(String message){
		logger.info("[Random Spawn] " + message);
	}

	public void logDebug(String message){
		if (yamlHandler.config.getBoolean("debug",false)) { logger.info("[Random Spawn] (DEBUG) " + message); }
	}

	public void logWarning(String message){
		logger.warning("[Random Spawn] " + message);
	}

	public void playerInfo(Player player, String message){
		player.sendMessage(ChatColor.AQUA + "[RandomSpawn] " + ChatColor.RESET + message);
	}

	// *------------------------------------------------------------------------------------------------------------*
	// | The random location methods contain code made by NuclearW                                                  |
	// | based on his SpawnArea plugin:                                                                             |
	// | http://forums.bukkit.org/threads/tp-spawnarea-v0-1-spawns-targetPlayers-in-a-set-area-randomly-1060.20408/ |
	// *------------------------------------------------------------------------------------------------------------*

	public Location chooseSpawn(World world){

		String worldName = world.getName();

		int xmin = yamlHandler.worlds.getInt(worldName +".spawnarea.x-min", -100);
		int xmax = yamlHandler.worlds.getInt(worldName +".spawnarea.x-max", 100);
		int zmin = yamlHandler.worlds.getInt(worldName +".spawnarea.z-min", -100);
		int zmax = yamlHandler.worlds.getInt(worldName +".spawnarea.z-max", 100);
		// Spawn area thickness near border. If 0 spawns whole area
		int thickness = yamlHandler.worlds.getInt(worldName +".spawnarea.thickness", 0);

		int xrand = 0;
		int zrand = 0;
		int y = -1;

		do {
			if (thickness <= 0) {
				xrand = xmin + (int) ( Math.random()*(xmax - xmin + 1) );
				zrand = zmin + (int) ( Math.random()*(zmax - zmin + 1) );
			}
			else {
				int side = (int) (Math.random() * 4d);
				int borderOffset = (int) (Math.random() * (double) thickness);
				if (side == 0) {
					xrand = xmin + borderOffset;
					// Also balancing probability considering thickness
					zrand = zmin + (int) ( Math.random() * (zmax - zmin + 1 - 2*thickness) ) + thickness;
				}
				else if (side == 1) {
					xrand = xmax - borderOffset;
					zrand = zmin + (int) ( Math.random() * (zmax - zmin + 1 - 2*thickness) ) + thickness;
				}
				else if (side == 2) {
					xrand = xmin + (int) ( Math.random() * (xmax - xmin + 1) );
					zrand = zmin + borderOffset;
				}
				else {
					xrand = xmin + (int) ( Math.random() * (xmax - xmin + 1) );
					zrand = zmax - borderOffset;
				}
				logDebug("Choosed side for spawn: " + side + "; x = " + xrand + "; z = " + zrand);
			}
			
			y = getValidHighestBlock(world, xrand, zrand);
		} while (y == -1);
		
		return new Location(
			world,
			Double.parseDouble(Integer.toString(xrand)),
			Double.parseDouble(Integer.toString(y)),
			Double.parseDouble(Integer.toString(zrand))
		);
	}

	private int getValidHighestBlock(World world, int x, int z) {
		world.getChunkAt(new Location(world, x, 0, z)).load();
		
		int y = world.getHighestBlockYAt(x, z);
		int blockid = world.getBlockTypeIdAt(x, y - 1, z);
		
		if (blockid == 8) return -1;
		if (blockid == 9) return -1;
		if (blockid == 10) return -1;
		if (blockid == 11) return -1;
		if (blockid == 51) return -1;
		if (blockid == 18) return -1;
		
		blockid = world.getBlockTypeIdAt(x, y + 1, z);
		
		if (blockid == 81) return -1;
		
		return y;
	}

	// Methods for a save landing :)

	public void sendGround(Player player, Location location){
		
		location.getChunk().load();
		
		World world = location.getWorld();
		
		for(int y = 0 ; y <= location.getBlockY() + 2; y++){
			Block block = world.getBlockAt(location.getBlockX(), y, location.getBlockZ());
			player.sendBlockChange(block.getLocation(), block.getType(), block.getData());
		}
	}
}
