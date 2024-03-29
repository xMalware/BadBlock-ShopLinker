package fr.badblock.common.shoplinker.bukkit.commands;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mongodb.BasicDBObject;
import com.mongodb.Cursor;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import fr.badblock.common.shoplinker.api.ShopLinkerAPI;
import fr.badblock.common.shoplinker.api.objects.ShopData;
import fr.badblock.common.shoplinker.api.objects.ShopType;
import fr.badblock.common.shoplinker.bukkit.ShopLinkWorker;
import fr.badblock.common.shoplinker.bukkit.ShopLinker;
import fr.badblock.common.shoplinker.bukkit.ShopLinkerLoader;
import fr.badblock.common.shoplinker.bukkit.clickers.ClickableObject;
import fr.badblock.common.shoplinker.bukkit.clickers.managers.SignManager;
import fr.badblock.common.shoplinker.bukkit.inventories.BukkitInventories;
import fr.badblock.common.shoplinker.bukkit.inventories.InventoriesLoader;
import fr.badblock.common.shoplinker.bukkit.listeners.bukkit.PlayerInteractListener;
import fr.badblock.common.shoplinker.mongodb.MongoService;
import net.md_5.bungee.api.ChatColor;

public class ShopLinkerCommand implements CommandExecutor {

	public static Map<UUID, String> armorSet = new HashMap<>();
	public static Map<UUID, String> removeArmor = new HashMap<>();

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
			help(sender, args);
			return true;
		}
		args[0] = args[0].toLowerCase();
		switch (args[0]) {
		case "opi":
		case "openinventory":
			opi(sender, args);
			break;
		case "reload":
		case "rl":
			reload(sender, args);
			break;
		case "rmsign":
		case "removesign":
		case "deletesign":
			rmSign(sender, args);
			break;
		case "setsign":
		case "addsign":
			addSign(sender, args);
			break;
		case "shopexecute":
		case "se":
			shopExecute(sender, args);
			break;
		case "armorset":
		case "armorstandset":
			armorSet(sender, args);
			break;
		case "removearmor":
		case "removearmorstand":
		case "rmarmorstand":
		case "rmarmor":
		case "deletearmor":
		case "deletearmorstand":
			removeArmor(sender, args);
			break;
		default:
			unknownCommand(sender, args);
		}
		return true;
	}

	private void help(CommandSender sender, String[] args) {
		if (notEnoughPermissions(sender)) return;
		sender.sendMessage(ChatColor.AQUA + "[ShopLinker] List of available commands :");
		sender.sendMessage(" ");
		sender.sendMessage(ChatColor.GRAY + "/shoplinker " + ChatColor.AQUA + "opi" + ChatColor.GRAY + " <inventoryName>" + ChatColor.RED + " > " + ChatColor.GRAY + "Open an inventory");
		sender.sendMessage(ChatColor.GRAY + "/shoplinker " + ChatColor.AQUA + "rl" + ChatColor.RED + " > " + ChatColor.GRAY + "Reload ShopLinker config");
		sender.sendMessage(ChatColor.GRAY + "/shoplinker " + ChatColor.AQUA + "rmsign" + ChatColor.RED + " > " + ChatColor.GRAY + "Remove a sign");
		sender.sendMessage(ChatColor.GRAY + "/shoplinker " + ChatColor.AQUA + "setsign" + ChatColor.GRAY + " <inventoryName>" + ChatColor.RED + " > " + ChatColor.GRAY + "Set a sign");
		sender.sendMessage(ChatColor.GRAY + "/shoplinker " + ChatColor.AQUA + "armorset" + ChatColor.RED + " <inventoryName> > " + ChatColor.GRAY + "Set an armor stand");
		sender.sendMessage(ChatColor.GRAY + "/shoplinker " + ChatColor.AQUA + "rmarmor" + ChatColor.RED + " <inventoryName> > " + ChatColor.GRAY + "Remove an armor stand");
		sender.sendMessage(ChatColor.GRAY + "/shoplinker " + ChatColor.AQUA + "shopexecute" + ChatColor.RED + " > " + ChatColor.GRAY + "Claim your features");
	}

	private void unknownCommand(CommandSender sender, String[] args) {
		sender.sendMessage(ChatColor.RED + "[ShopLinker] Unknown command. Type \"/help\".");
	}

	private void opi(CommandSender sender, String[] args) {
		if (neededPlayer(sender)) return;
		if (args.length != 2) {
			sender.sendMessage(ChatColor.RED + "[ShopLinker] Usage: /opi <inventoryName>");
			return;
		}
		String inventoryName = args[1];
		Player player = (Player) sender;
		BukkitInventories.openInventory(player, inventoryName);
	}

	private void reload(CommandSender sender, String[] args) {
		if (notEnoughPermissions(sender)) return;
		ShopLinker shopLinker = ShopLinker.getInstance();
		InventoriesLoader.reloadInventories(shopLinker);
		new ShopLinkerLoader(shopLinker);
		sender.sendMessage(ChatColor.GREEN + "[ShopLinker] Reloaded configuration!");
	}

	private void rmSign(CommandSender sender, String[] args) {
		if (neededPlayer(sender)) return;
		if (notEnoughPermissions(sender)) return;
		Player player = (Player) sender;
		List<Block> blocks = player.getLineOfSight(new HashSet<Material>(), 500);
		Block b = null;
		for (Block block : blocks) {
			if (block != null && PlayerInteractListener.supportedMaterialList.contains(block.getType())) {
				b = block;
				break;
			}
		}
		if (b == null) {
			sender.sendMessage(ChatColor.RED + "[ShopLinker] You must be in front of a sign.");
			return;
		}
		SignManager signManager = SignManager.getInstance();
		signManager.removeSign(b.getLocation());
		signManager.save();
	}

	private void addSign(CommandSender sender, String[] args) {
		if (neededPlayer(sender)) return;
		if (notEnoughPermissions(sender)) return;
		if (args.length != 2) {
			sender.sendMessage(ChatColor.RED + "[ShopLinker] Usage: /setsign <inventoryName>");
			return;
		}
		Player player = (Player) sender;
		List<Block> blocks = player.getLineOfSight(new HashSet<Material>(), 200);
		Block b = null;
		for (Block block : blocks) {
			if (block != null && !block.getType().equals(Material.AIR)) {
				b = block;
				break;
			}
		}
		if (b == null) {
			sender.sendMessage(ChatColor.RED + "[ShopLinker] You must be in front of a sign.");
			return;
		}
		SignManager signManager = SignManager.getInstance();
		String inventoryName = args[1];
		ClickableObject signObject = new ClickableObject(b.getLocation(), inventoryName);	
		signManager.setSign(signObject);
		signManager.save();
		sender.sendMessage(ChatColor.GREEN + "[ShopLinker] You set the sign as an inventory opener.");
	}

	private void shopExecute(CommandSender sender, String[] args) {
		if (neededPlayer(sender)) return;
		Player player = (Player) sender;

		new Thread()
		{
			@Override
			public void run()
			{
				ShopLinker shopLinker = ShopLinker.getInstance();
				MongoService mongoService = shopLinker.getMongoService();

				String serverName = ShopLinkerAPI.CURRENT_SERVER_NAME;
				String playerName = player.getName().toLowerCase();

				DB db = mongoService.getDb();
				DBCollection collection = db.getCollection("cachedShop");

				BasicDBObject object = new BasicDBObject();
				object.put("playerName", playerName);
				object.put("serverName", serverName);

				Cursor cursor = collection.find(object);

				int count = 0;
				while (cursor.hasNext())
				{
					count++;
					
					DBObject obj = cursor.next();
					collection.remove(obj);

					String type = (String) obj.get("type");
					String playerNm = (String) obj.get("playerName");
					String command = (String) obj.get("command");
					String displayName = (String) obj.get("displayName");
					boolean ingame = (boolean) obj.get("ingame");
					double price = (double) obj.get("price");

					Bukkit.getScheduler().runTask(shopLinker, new Runnable()
					{
						@Override
						public void run()
						{
							ShopLinkWorker.workCommand(new ShopData(ShopType.getFrom(type), playerNm, command, displayName, new int[] {}, false, ingame, price, false), true);
						}
					});
				}

				String message = count > 1 ? shopLinker.getPluralClaimMessage().replace("%0", Integer.toString(count)) : shopLinker.getSingleClaimMessage();
				if (count > 0)
				{
					player.sendMessage(message);
					return;
				}
				
				player.sendMessage(ShopLinker.getInstance().getNothingToClaimMessage());
			}
		}.start();
	}

	private void armorSet(CommandSender sender, String[] args) {
		if (neededPlayer(sender)) return;
		if (notEnoughPermissions(sender)) return;
		Player player = (Player) sender;
		if (args.length != 2) {
			sender.sendMessage(ChatColor.RED + "[ShopLinker] Usage: /armorset <inventoryName>");
			return;
		}
		UUID uniquePlayerId = player.getUniqueId();
		if (!armorSet.containsKey(uniquePlayerId)) {
			armorSet.put(uniquePlayerId, args[1]);
			sender.sendMessage(ChatColor.GREEN + "[ShopLinker] Ok. Now, right click on the armor stand.");
		}else {
			armorSet.remove(uniquePlayerId);
			sender.sendMessage(ChatColor.RED + "[ShopLinker] You cancelled this operation.");
		}
	}

	private void removeArmor(CommandSender sender, String[] args) {
		if (neededPlayer(sender)) return;
		if (notEnoughPermissions(sender)) return;
		Player player = (Player) sender;
		if (args.length != 1) {
			sender.sendMessage(ChatColor.RED + "[ShopLinker] Usage: /rmarmor");
			return;
		}
		UUID uniquePlayerId = player.getUniqueId();
		if (!removeArmor.containsKey(uniquePlayerId)) {
			removeArmor.put(uniquePlayerId, args[1]);
			sender.sendMessage(ChatColor.GREEN + "[ShopLinker] Ok. Now, right click on the armor stand.");
		}else {
			removeArmor.remove(uniquePlayerId);
			sender.sendMessage(ChatColor.RED + "[ShopLinker] You cancelled this operation.");
		}
	}

	private boolean notEnoughPermissions(CommandSender sender) {
		if (!sender.hasPermission("shoplinker.admin")) {
			sender.sendMessage(ChatColor.RED + "[ShopLinker] You don't have enough permission.");
			return true;
		}
		return false;
	}

	private boolean neededPlayer(CommandSender sender) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "[ShopLinker] You must be a player to execute this.");
			return true;
		}
		return false;
	}

}
