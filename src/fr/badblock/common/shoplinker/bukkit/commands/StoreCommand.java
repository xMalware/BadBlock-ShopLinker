package fr.badblock.common.shoplinker.bukkit.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.badblock.common.shoplinker.bukkit.inventories.BukkitInventories;

public class StoreCommand implements CommandExecutor
{

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		if (!(sender instanceof Player))
		{
			return true;
		}
		
		BukkitInventories.openInventory((Player) sender, "5c729b9a1e474027d4598774");
		
		return true;
	}
	
}
